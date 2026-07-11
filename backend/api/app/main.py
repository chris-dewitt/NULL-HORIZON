"""FastAPI application entrypoint.

This process serves HTTP only. Learner-authored code is never imported, compiled,
or executed here.
"""

from __future__ import annotations

from fastapi import FastAPI

from api.app.core.config import Settings, get_settings
from api.app.core.health import HealthResponse


def create_app(settings: Settings | None = None) -> FastAPI:
    """Create the API application instance."""

    resolved = settings or get_settings()
    application = FastAPI(
        title=resolved.app_name,
        version=resolved.api_version,
        docs_url="/docs",
        redoc_url="/redoc",
    )

    @application.get("/v1/health", response_model=HealthResponse, tags=["health"])
    def health() -> HealthResponse:
        """Return liveness information for the API process."""

        return HealthResponse(
            service=resolved.app_name,
            version=resolved.api_version,
            execution_provider=resolved.execution_provider,
        )

    return application


app = create_app()
