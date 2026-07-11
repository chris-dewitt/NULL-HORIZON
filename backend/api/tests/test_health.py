"""Health and OpenAPI surface tests."""

from __future__ import annotations

import json
from pathlib import Path

from api.tests.conftest import make_client


def test_health_returns_ok() -> None:
    client = make_client()
    response = client.get("/v1/health")
    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "ok"
    assert payload["service"] == "NULL HORIZON API"
    assert payload["version"] == "0.1.0"
    assert payload["execution_provider"] == "fake"
    assert payload["database"] == "ok"
    assert payload["redis"] == "memory"


def test_openapi_includes_epic7_routes() -> None:
    client = make_client()
    openapi = client.get("/openapi.json").json()
    paths = set(openapi["paths"])
    assert "/v1/health" in paths
    assert "/v1/content/manifest" in paths
    assert "/v1/profiles/anonymous" in paths
    assert "/v1/profiles/me" in paths
    assert "/v1/progress" in paths
    assert "/v1/progress/sync" in paths
    assert "/v1/executions" in paths
    assert "/v1/executions/{execution_id}" in paths

    out = Path(__file__).resolve().parents[3] / "shared" / "openapi" / "openapi.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(openapi, indent=2) + "\n", encoding="utf-8")
