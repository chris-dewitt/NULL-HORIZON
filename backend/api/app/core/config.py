"""Application settings.

Secrets must come from the environment or a secret manager, never from the repository.
"""

from __future__ import annotations

from pydantic import BaseModel, Field


class Settings(BaseModel):
    """Runtime settings for the API process."""

    app_name: str = "NULL HORIZON API"
    api_version: str = "0.0.1"
    execution_provider: str = Field(
        default="fake",
        description=(
            "Execution backend selector. The API process never runs learner code; "
            "providers are adapters to isolated runners."
        ),
    )


def get_settings() -> Settings:
    """Return process settings.

    Environment overrides are intentionally minimal in Epic 0.
    """

    import os

    return Settings(
        execution_provider=os.getenv("EXECUTION_PROVIDER", "fake"),
    )
