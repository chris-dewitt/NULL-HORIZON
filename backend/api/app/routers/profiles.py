"""Anonymous profile routes."""

from __future__ import annotations

import secrets
from typing import Annotated

from fastapi import APIRouter, Depends, Response, status
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from api.app.core.security import hash_token, mint_access_token
from api.app.db.models import (
    ExecutionJob,
    ExecutionResultRow,
    MissionProgress,
    Profile,
    ProgressSnapshot,
    RewardUnlock,
    SkillEvidence,
)
from api.app.deps import get_current_profile, get_db
from api.app.schemas import AnonymousProfileRequest, AnonymousProfileResponse

router = APIRouter(tags=["profiles"])


@router.post("/v1/profiles/anonymous", response_model=AnonymousProfileResponse)
def create_anonymous_profile(
    body: AnonymousProfileRequest,
    session: Annotated[Session, Depends(get_db)],
) -> AnonymousProfileResponse:
    token = mint_access_token()
    profile = Profile(
        id=f"prf_{secrets.token_urlsafe(12)}",
        display_name=body.display_name.strip(),
        access_token_hash=hash_token(token),
    )
    session.add(profile)
    session.flush()
    return AnonymousProfileResponse(
        profile_id=profile.id,
        display_name=profile.display_name,
        access_token=token,
    )


def _delete_profile_owned_rows(session: Session, profile_id: str) -> None:
    job_ids = list(
        session.scalars(
            select(ExecutionJob.id).where(ExecutionJob.profile_id == profile_id)
        )
    )
    if job_ids:
        session.execute(
            delete(ExecutionResultRow).where(
                ExecutionResultRow.execution_id.in_(job_ids)
            )
        )
        session.execute(
            delete(ExecutionJob).where(ExecutionJob.profile_id == profile_id)
        )
    session.execute(
        delete(MissionProgress).where(MissionProgress.profile_id == profile_id)
    )
    session.execute(
        delete(SkillEvidence).where(SkillEvidence.profile_id == profile_id)
    )
    session.execute(
        delete(RewardUnlock).where(RewardUnlock.profile_id == profile_id)
    )
    session.execute(
        delete(ProgressSnapshot).where(ProgressSnapshot.profile_id == profile_id)
    )


@router.delete("/v1/profiles/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_profile(
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> Response:
    """Delete the authenticated profile and all owned cloud data."""
    _delete_profile_owned_rows(session, profile.id)
    session.delete(profile)
    session.flush()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
