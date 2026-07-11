"""Health-check response models."""

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    """Public health payload for load balancers and local checks."""

    status: Literal["ok"] = "ok"
    service: str = Field(description="Human-readable service name")
    version: str = Field(description="API package version")
    execution_provider: str = Field(
        description=(
            "Configured execution provider name. Does not imply in-process execution."
        )
    )
