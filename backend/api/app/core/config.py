"""Application settings.

Secrets must come from the environment or a secret manager, never from the repository.
"""

from __future__ import annotations

from functools import lru_cache
from pathlib import Path

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Runtime settings for the API process."""

    model_config = SettingsConfigDict(
        env_file=".env",
        extra="ignore",
        populate_by_name=True,
    )

    app_name: str = "NULL HORIZON API"
    api_version: str = "0.1.0"
    execution_provider: str = Field(
        default="fake",
        validation_alias="EXECUTION_PROVIDER",
        description=(
            "Execution backend: fake | local_trusted | hardened. "
            "API never runs learner code; adapters call isolated runners. "
            "hardened remains blocked until security review."
        ),
    )
    database_url: str = Field(
        default="sqlite+pysqlite:///:memory:",
        validation_alias="DATABASE_URL",
    )
    redis_url: str = Field(default="", validation_alias="REDIS_URL")
    content_bundle_dir: str = Field(
        default="",
        validation_alias="CONTENT_BUNDLE_DIR",
        description="Directory containing compiled content manifest.json",
    )
    idempotency_ttl_seconds: int = 86_400

    def resolved_content_dir(self) -> Path:
        if self.content_bundle_dir:
            return Path(self.content_bundle_dir)
        # Monorepo default: content/build/bundles/dev relative to backend/
        return (
            Path(__file__).resolve().parents[4]
            / "content"
            / "build"
            / "bundles"
            / "dev"
        )


@lru_cache
def get_settings() -> Settings:
    return Settings()
