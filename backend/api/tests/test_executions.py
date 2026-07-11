"""Execution job API tests using the fake provider."""

from __future__ import annotations

from api.tests.conftest import create_profile, make_client


def test_execution_submit_get_and_idempotency() -> None:
    client = make_client()
    profile = create_profile(client)
    headers = {
        "Authorization": f"Bearer {profile['access_token']}",
        "Idempotency-Key": "exe-1",
    }
    body = {
        "mission_id": "automation.pressure_threshold.01",
        "mission_version": "1.0.0",
        "runtime": "python",
        "environment_id": "python.editor.fake.v1",
        "files": [
            {
                "path": "/workspace/pressure.py",
                "content": "THRESHOLD = 50\n",
            }
        ],
    }
    first = client.post("/v1/executions", json=body, headers=headers)
    second = client.post("/v1/executions", json=body, headers=headers)
    assert first.status_code == 200
    assert second.status_code == 200
    assert first.json() == second.json()
    execution_id = first.json()["execution_id"]

    result = client.get(
        f"/v1/executions/{execution_id}",
        headers={"Authorization": f"Bearer {profile['access_token']}"},
    )
    assert result.status_code == 200
    payload = result.json()
    assert payload["status"] == "completed"
    assert payload["outcome"] == "passed"
    assert all(t["status"] == "passed" for t in payload["tests"])


def test_execution_requires_auth() -> None:
    client = make_client()
    response = client.post(
        "/v1/executions",
        json={
            "idempotency_key": "x",
            "mission_id": "m",
            "mission_version": "1.0.0",
            "environment_id": "e",
            "files": [],
        },
    )
    assert response.status_code == 401


def test_content_manifest_available() -> None:
    client = make_client()
    response = client.get("/v1/content/manifest")
    assert response.status_code == 200
    assert response.json()["bundle_id"] == "null-horizon-starter"
