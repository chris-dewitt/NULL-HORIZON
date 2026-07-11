"""Anonymous profile routes."""

from __future__ import annotations

import secrets
from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from api.app.core.security import hash_token, mint_access_token
from api.app.db.models import Profile
from api.app.deps import get_db
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
