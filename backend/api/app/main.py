"""FastAPI application entrypoint.

This process serves HTTP only. Learner-authored code is never imported, compiled,
or executed here.
"""

from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Any

from fastapi import FastAPI

from api.app.core.config import Settings, get_settings
from api.app.db.base import Base, create_db_engine, create_session_factory
from api.app.providers.factory import build_api_execution_provider
from api.app.redis.client import create_kv_store
from api.app.routers import content, executions, health, profiles, progress


def create_app(settings: Settings | None = None) -> FastAPI:
    """Create the API application instance."""

    resolved = settings or get_settings()
    engine = create_db_engine(resolved.database_url)
    session_factory = create_session_factory(engine)
    kv_store = create_kv_store(resolved.redis_url)
    provider = build_api_execution_provider(resolved.execution_provider)

    @asynccontextmanager
    async def lifespan(application: FastAPI):
        Base.metadata.create_all(bind=engine)
        application.state.settings = resolved
        application.state.engine = engine
        application.state.session_factory = session_factory
        application.state.kv_store = kv_store
        application.state.execution_provider = provider
        yield
        engine.dispose()

    application = FastAPI(
        title=resolved.app_name,
        version=resolved.api_version,
        docs_url="/docs",
        redoc_url="/redoc",
        lifespan=lifespan,
    )
    application.include_router(health.router)
    application.include_router(content.router)
    application.include_router(profiles.router)
    application.include_router(progress.router)
    application.include_router(executions.router)

    # Ensure state exists for TestClient before lifespan in some contexts.
    application.state.settings = resolved
    application.state.engine = engine
    application.state.session_factory = session_factory
    application.state.kv_store = kv_store
    application.state.execution_provider = provider
    Base.metadata.create_all(bind=engine)

    return application


app = create_app()


def export_openapi() -> dict[str, Any]:
    return create_app().openapi()
