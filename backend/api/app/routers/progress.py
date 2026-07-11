"""Progress read/write and sync routes with Epic 9 merge rules."""

from __future__ import annotations

import secrets
import time
from collections import defaultdict
from datetime import UTC, datetime
from typing import Annotated

from fastapi import APIRouter, Depends, Header
from sqlalchemy import select
from sqlalchemy.orm import Session

from api.app.core.idempotency import IdempotencyService
from api.app.db.models import MissionProgress, Profile, RewardUnlock, SkillEvidence
from api.app.deps import get_current_profile, get_db, get_idempotency
from api.app.progression.engine import (
    mastery_level,
    rank_for_clearance,
    review_recommendations,
)
from api.app.schemas import (
    MissionProgressItem,
    ProgressResponse,
    ProgressSyncRequest,
    ProgressSyncResponse,
    ReviewRecommendationItem,
    RewardItem,
    SkillEvidenceItem,
    SkillMasteryItem,
    UpsertMissionProgressRequest,
)

router = APIRouter(tags=["progress"])


def _mission_items(session: Session, profile_id: str) -> list[MissionProgressItem]:
    rows = session.scalars(
        select(MissionProgress).where(MissionProgress.profile_id == profile_id)
    ).all()
    return [
        MissionProgressItem(
            mission_id=row.mission_id,
            status=row.status,
            best_hint_level=row.best_hint_level,
            clearance_awarded=row.clearance_awarded,
            attempt_count=row.attempt_count,
            mission_version=row.mission_version,
        )
        for row in rows
    ]


def _skill_items(session: Session, profile_id: str) -> list[SkillMasteryItem]:
    rows = session.scalars(
        select(SkillEvidence).where(SkillEvidence.profile_id == profile_id)
    ).all()
    totals: dict[str, dict[str, int]] = defaultdict(
        lambda: {"evidence": 0, "unassisted": 0, "last": 0}
    )
    for row in rows:
        bucket = totals[row.skill_id]
        bucket["evidence"] += row.delta
        if not row.assisted:
            bucket["unassisted"] += row.delta
        stamp = int(row.created_at.timestamp() * 1000)
        bucket["last"] = max(bucket["last"], stamp)
    return [
        SkillMasteryItem(
            skill_id=skill_id,
            mastery_level=mastery_level(
                evidence_count=vals["evidence"],
                unassisted_evidence_count=vals["unassisted"],
            ),
            evidence_count=vals["evidence"],
            unassisted_evidence_count=vals["unassisted"],
            last_practiced_at_epoch_ms=vals["last"],
        )
        for skill_id, vals in sorted(totals.items())
    ]


def _reward_items(session: Session, profile_id: str) -> list[RewardItem]:
    rows = session.scalars(
        select(RewardUnlock).where(RewardUnlock.profile_id == profile_id)
    ).all()
    return [RewardItem(reward_id=row.reward_id, equipped=row.equipped) for row in rows]


def _review_items(skills: list[SkillMasteryItem]) -> list[ReviewRecommendationItem]:
    now = int(time.time() * 1000)
    recs = review_recommendations(
        [s.model_dump() for s in skills],
        now_epoch_ms=now,
    )
    return [
        ReviewRecommendationItem(skill_id=r.skill_id, reason=r.reason) for r in recs
    ]


def _progress_payload(session: Session, profile: Profile) -> ProgressResponse:
    skills = _skill_items(session, profile.id)
    return ProgressResponse(
        profile_id=profile.id,
        rank=profile.rank,
        clearance_points=profile.clearance_points,
        missions=_mission_items(session, profile.id),
        skills=skills,
        rewards=_reward_items(session, profile.id),
        review_recommendations=_review_items(skills),
    )


def _upsert_mission(
    session: Session,
    profile: Profile,
    item: MissionProgressItem,
) -> bool:
    """Upsert mission progress. Returns True when a new completion row is created."""
    existing = session.scalar(
        select(MissionProgress).where(
            MissionProgress.profile_id == profile.id,
            MissionProgress.mission_id == item.mission_id,
        )
    )
    now = datetime.now(UTC)
    if existing is None:
        awarded = max(item.clearance_awarded, 0)
        session.add(
            MissionProgress(
                id=f"mp_{secrets.token_urlsafe(10)}",
                profile_id=profile.id,
                mission_id=item.mission_id,
                status=item.status,
                best_hint_level=item.best_hint_level,
                clearance_awarded=awarded,
                attempt_count=max(item.attempt_count, 1),
                mission_version=item.mission_version,
                completed_at=now,
                updated_at=now,
            )
        )
        profile.clearance_points += awarded
        profile.rank = rank_for_clearance(profile.clearance_points)
        return True
    # Monotonic completion + least-assisted hint level wins.
    existing.status = "completed"
    existing.best_hint_level = min(existing.best_hint_level, item.best_hint_level)
    existing.attempt_count = max(existing.attempt_count, item.attempt_count)
    existing.mission_version = item.mission_version or existing.mission_version
    # Clearance is awarded once per mission.
    if existing.clearance_awarded == 0 and item.clearance_awarded > 0:
        existing.clearance_awarded = item.clearance_awarded
        profile.clearance_points += item.clearance_awarded
        profile.rank = rank_for_clearance(profile.clearance_points)
    existing.updated_at = now
    return False


def _upsert_evidence(
    session: Session,
    profile: Profile,
    item: SkillEvidenceItem,
) -> bool:
    existing = session.scalar(
        select(SkillEvidence).where(
            SkillEvidence.profile_id == profile.id,
            SkillEvidence.event_id == item.event_id,
        )
    )
    if existing is not None:
        return False
    session.add(
        SkillEvidence(
            id=f"se_{secrets.token_urlsafe(10)}",
            profile_id=profile.id,
            event_id=item.event_id,
            skill_id=item.skill_id,
            mission_id=item.mission_id,
            assisted=item.assisted,
            delta=item.delta,
            created_at=datetime.now(UTC),
        )
    )
    return True


def _upsert_reward(
    session: Session,
    profile: Profile,
    item: RewardItem,
) -> bool:
    existing = session.scalar(
        select(RewardUnlock).where(
            RewardUnlock.profile_id == profile.id,
            RewardUnlock.reward_id == item.reward_id,
        )
    )
    if existing is not None:
        existing.equipped = existing.equipped or item.equipped
        return False
    session.add(
        RewardUnlock(
            id=f"rw_{secrets.token_urlsafe(10)}",
            profile_id=profile.id,
            reward_id=item.reward_id,
            unlocked_at=datetime.now(UTC),
            equipped=item.equipped,
        )
    )
    return True


@router.get("/v1/progress", response_model=ProgressResponse)
def get_progress(
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> ProgressResponse:
    return _progress_payload(session, profile)


@router.put("/v1/progress/missions/{mission_id}", response_model=MissionProgressItem)
def put_mission_progress(
    mission_id: str,
    body: UpsertMissionProgressRequest,
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> MissionProgressItem:
    item = MissionProgressItem(
        mission_id=mission_id,
        status=body.status,
        best_hint_level=body.best_hint_level,
        clearance_awarded=body.clearance_awarded,
        attempt_count=body.attempt_count,
        mission_version=body.mission_version,
    )
    _upsert_mission(session, profile, item)
    session.flush()
    return item


@router.post("/v1/progress/sync", response_model=ProgressSyncResponse)
def sync_progress(
    body: ProgressSyncRequest,
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
    idem: Annotated[IdempotencyService, Depends(get_idempotency)],
    idempotency_key: Annotated[str | None, Header(alias="Idempotency-Key")] = None,
) -> ProgressSyncResponse:
    key = idem.require_key(idempotency_key or body.idempotency_key)
    cached = idem.get_cached("progress_sync", profile.id, key)
    if cached is not None:
        return ProgressSyncResponse.model_validate(cached)

    accepted = 0
    for mission in body.missions:
        _upsert_mission(session, profile, mission)
        accepted += 1
    for evidence in body.skill_evidence:
        _upsert_evidence(session, profile, evidence)
        accepted += 1
    for reward in body.rewards:
        _upsert_reward(session, profile, reward)
        accepted += 1
    session.flush()
    payload = _progress_payload(session, profile)
    response = ProgressSyncResponse(
        profile_id=payload.profile_id,
        accepted=accepted,
        rank=payload.rank,
        clearance_points=payload.clearance_points,
        missions=payload.missions,
        skills=payload.skills,
        rewards=payload.rewards,
        review_recommendations=payload.review_recommendations,
    )
    idem.put("progress_sync", profile.id, key, response.model_dump())
    return response
