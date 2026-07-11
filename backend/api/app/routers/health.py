"""Health router."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, Request
from sqlalchemy import text
from sqlalchemy.orm import Session

from api.app.deps import get_db, get_kv
from api.app.redis.client import KeyValueStore, MemoryKeyValueStore
from api.app.schemas import HealthResponse

router = APIRouter(tags=["health"])


@router.get("/v1/health", response_model=HealthResponse)
def health(
    request: Request,
    session: Annotated[Session, Depends(get_db)],
    kv: Annotated[KeyValueStore, Depends(get_kv)],
) -> HealthResponse:
    settings = request.app.state.settings
    db_status = "ok"
    try:
        session.execute(text("SELECT 1"))
    except Exception:
        db_status = "error"

    redis_status: str
    try:
        ok = kv.ping()
        redis_status = (
            "memory"
            if isinstance(kv, MemoryKeyValueStore)
            else ("ok" if ok else "error")
        )
    except Exception:
        redis_status = "error"

    return HealthResponse(
        service=settings.app_name,
        version=settings.api_version,
        execution_provider=settings.execution_provider,
        database=db_status,  # type: ignore[arg-type]
        redis=redis_status,  # type: ignore[arg-type]
    )
