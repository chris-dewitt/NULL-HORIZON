"""Opaque bearer-token auth for anonymous profiles."""

from __future__ import annotations

import hashlib
import secrets
from typing import Annotated

from fastapi import Header, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from api.app.db.models import Profile


def hash_token(token: str) -> str:
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def mint_access_token() -> str:
    return f"nh_anon_{secrets.token_urlsafe(24)}"


def require_profile(
    session: Session,
    authorization: Annotated[str | None, Header()] = None,
) -> Profile:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": {"code": "UNAUTHORIZED", "message": "Bearer token required"}
            },
        )
    token = authorization.split(" ", 1)[1].strip()
    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": {"code": "UNAUTHORIZED", "message": "Bearer token required"}
            },
        )
    profile = session.scalar(
        select(Profile).where(Profile.access_token_hash == hash_token(token))
    )
    if profile is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"error": {"code": "UNAUTHORIZED", "message": "Invalid token"}},
        )
    return profile
