"""Authorization and progress sync tests."""

from __future__ import annotations

from api.tests.conftest import create_profile, make_client


def test_progress_requires_bearer_token() -> None:
    client = make_client()
    response = client.get("/v1/progress")
    assert response.status_code == 401


def test_invalid_token_is_rejected() -> None:
    client = make_client()
    response = client.get(
        "/v1/progress", headers={"Authorization": "Bearer not-a-token"}
    )
    assert response.status_code == 401


def test_progress_sync_is_idempotent() -> None:
    client = make_client()
    profile = create_profile(client)
    headers = {
        "Authorization": f"Bearer {profile['access_token']}",
        "Idempotency-Key": "sync-1",
    }
    body = {
        "missions": [
            {
                "mission_id": "automation.pressure_threshold.01",
                "status": "completed",
                "best_hint_level": 2,
            }
        ]
    }
    first = client.post("/v1/progress/sync", json=body, headers=headers)
    second = client.post("/v1/progress/sync", json=body, headers=headers)
    assert first.status_code == 200
    assert second.status_code == 200
    assert first.json() == second.json()
    assert first.json()["accepted"] == 1

    listed = client.get(
        "/v1/progress", headers={"Authorization": f"Bearer {profile['access_token']}"}
    )
    assert listed.status_code == 200
    assert (
        listed.json()["missions"][0]["mission_id"] == "automation.pressure_threshold.01"
    )
