"""Progress read/write and sync routes."""

from __future__ import annotations

import secrets
from datetime import UTC, datetime
from typing import Annotated

from fastapi import APIRouter, Depends, Header
from sqlalchemy import select
from sqlalchemy.orm import Session

from api.app.core.idempotency import IdempotencyService
from api.app.db.models import MissionProgress, Profile
from api.app.deps import get_current_profile, get_db, get_idempotency
from api.app.schemas import (
    MissionProgressItem,
    ProgressResponse,
    ProgressSyncRequest,
    ProgressSyncResponse,
    UpsertMissionProgressRequest,
)

router = APIRouter(tags=["progress"])


def _items_for_profile(session: Session, profile_id: str) -> list[MissionProgressItem]:
    rows = session.scalars(
        select(MissionProgress).where(MissionProgress.profile_id == profile_id)
    ).all()
    return [
        MissionProgressItem(
            mission_id=row.mission_id,
            status=row.status,
            best_hint_level=row.best_hint_level,
        )
        for row in rows
    ]


def _upsert_mission(
    session: Session,
    profile: Profile,
    item: MissionProgressItem,
) -> None:
    existing = session.scalar(
        select(MissionProgress).where(
            MissionProgress.profile_id == profile.id,
            MissionProgress.mission_id == item.mission_id,
        )
    )
    now = datetime.now(UTC)
    if existing is None:
        session.add(
            MissionProgress(
                id=f"mp_{secrets.token_urlsafe(10)}",
                profile_id=profile.id,
                mission_id=item.mission_id,
                status=item.status,
                best_hint_level=item.best_hint_level,
                completed_at=now,
                updated_at=now,
            )
        )
        return
    # Monotonic completion + least-assisted hint level wins.
    existing.status = "completed"
    existing.best_hint_level = min(existing.best_hint_level, item.best_hint_level)
    existing.updated_at = now


@router.get("/v1/progress", response_model=ProgressResponse)
def get_progress(
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> ProgressResponse:
    return ProgressResponse(
        profile_id=profile.id, missions=_items_for_profile(session, profile.id)
    )


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

    for item in body.missions:
        _upsert_mission(session, profile, item)
    session.flush()
    response = ProgressSyncResponse(
        profile_id=profile.id,
        accepted=len(body.missions),
        missions=_items_for_profile(session, profile.id),
    )
    idem.put("progress_sync", profile.id, key, response.model_dump())
    return response
