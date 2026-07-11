"""FastAPI dependency wiring."""

from __future__ import annotations

from collections.abc import Generator
from typing import Annotated

from fastapi import Depends, Header, Request
from sqlalchemy.orm import Session

from api.app.core.idempotency import IdempotencyService
from api.app.core.security import require_profile
from api.app.db.models import Profile
from api.app.providers.fake import FakeExecutionProvider
from api.app.redis.client import KeyValueStore


def get_db(request: Request) -> Generator[Session, None, None]:
    factory = request.app.state.session_factory
    session = factory()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()


def get_kv(request: Request) -> KeyValueStore:
    return request.app.state.kv_store


def get_idempotency(request: Request) -> IdempotencyService:
    return IdempotencyService(
        request.app.state.kv_store,
        ttl_seconds=request.app.state.settings.idempotency_ttl_seconds,
    )


def get_execution_provider(request: Request) -> FakeExecutionProvider:
    return request.app.state.execution_provider


def get_current_profile(
    session: Annotated[Session, Depends(get_db)],
    authorization: Annotated[str | None, Header()] = None,
) -> Profile:
    return require_profile(session, authorization)
