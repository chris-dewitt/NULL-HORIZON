"""Shared test client helpers."""

from __future__ import annotations

from pathlib import Path

from api.app.core.config import Settings
from api.app.main import create_app
from fastapi.testclient import TestClient

CONTENT_DIR = (
    Path(__file__).resolve().parents[3] / "content" / "build" / "bundles" / "dev"
)


def make_client() -> TestClient:
    settings = Settings(
        database_url="sqlite+pysqlite:///:memory:",
        redis_url="",
        content_bundle_dir=str(CONTENT_DIR),
        api_version="0.1.0",
        execution_provider="fake",
    )
    return TestClient(create_app(settings))


def create_profile(client: TestClient, name: str = "Operator") -> dict[str, str]:
    response = client.post("/v1/profiles/anonymous", json={"display_name": name})
    assert response.status_code == 200
    return response.json()
