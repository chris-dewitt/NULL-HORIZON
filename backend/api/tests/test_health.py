"""Health endpoint tests for the Epic 0 API shell."""

from __future__ import annotations

from api.app.core.config import Settings
from api.app.main import create_app
from fastapi.testclient import TestClient


def test_health_returns_ok() -> None:
    client = TestClient(create_app(Settings(execution_provider="fake")))

    response = client.get("/v1/health")

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "ok"
    assert payload["service"] == "NULL HORIZON API"
    assert payload["version"] == "0.0.1"
    assert payload["execution_provider"] == "fake"


def test_health_reflects_configured_provider() -> None:
    client = TestClient(create_app(Settings(execution_provider="fake")))

    payload = client.get("/v1/health").json()

    assert payload["execution_provider"] == "fake"


def test_api_process_does_not_expose_execution_run_route() -> None:
    """Epic 0 must not imply in-process learner execution."""

    client = TestClient(create_app())
    openapi = client.get("/openapi.json").json()
    paths = set(openapi["paths"])

    assert "/v1/health" in paths
    assert "/v1/executions" not in paths
