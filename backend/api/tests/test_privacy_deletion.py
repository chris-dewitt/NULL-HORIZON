"""Privacy deletion API tests for Epic 13."""

from __future__ import annotations

from api.tests.conftest import create_profile, make_client


def test_delete_progress_keeps_profile() -> None:
    client = make_client()
    profile = create_profile(client)
    headers = {"Authorization": f"Bearer {profile['access_token']}"}
    sync = client.post(
        "/v1/progress/sync",
        json={
            "missions": [
                {
                    "mission_id": "emergency.lighting.01",
                    "status": "completed",
                    "best_hint_level": 0,
                }
            ]
        },
        headers={**headers, "Idempotency-Key": "del-progress-1"},
    )
    assert sync.status_code == 200

    deleted = client.delete("/v1/progress", headers=headers)
    assert deleted.status_code == 204

    listed = client.get("/v1/progress", headers=headers)
    assert listed.status_code == 200
    body = listed.json()
    assert body["missions"] == []
    assert body["clearance_points"] == 0


def test_delete_profile_revokes_access() -> None:
    client = make_client()
    profile = create_profile(client)
    headers = {"Authorization": f"Bearer {profile['access_token']}"}

    deleted = client.delete("/v1/profiles/me", headers=headers)
    assert deleted.status_code == 204

    listed = client.get("/v1/progress", headers=headers)
    assert listed.status_code == 401
