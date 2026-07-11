"""Progression engine and sync acceptance tests."""

from __future__ import annotations

from api.app.progression.engine import mastery_level, rank_for_clearance
from api.tests.conftest import create_profile, make_client


def test_mastery_and_rank_helpers() -> None:
    assert mastery_level(evidence_count=0, unassisted_evidence_count=0) == "none"
    assert mastery_level(evidence_count=1, unassisted_evidence_count=0) == "introduced"
    assert mastery_level(evidence_count=2, unassisted_evidence_count=0) == "practiced"
    assert mastery_level(evidence_count=2, unassisted_evidence_count=2) == "reliable"
    assert mastery_level(evidence_count=3, unassisted_evidence_count=3) == "mastered"
    assert rank_for_clearance(0) == "Emergency Operator"
    assert rank_for_clearance(50) == "Maintenance Technician"


def test_progress_sync_applies_mastery_rewards_idempotently() -> None:
    client = make_client()
    profile = create_profile(client)
    token = profile["access_token"]
    headers = {
        "Authorization": f"Bearer {token}",
        "Idempotency-Key": "prog-sync-1",
    }
    body = {
        "missions": [
            {
                "mission_id": "emergency.lighting.01",
                "status": "completed",
                "best_hint_level": 2,
                "clearance_awarded": 25,
                "attempt_count": 1,
                "mission_version": "1.0.0",
            }
        ],
        "skill_evidence": [
            {
                "event_id": "emergency.lighting.01:linux.navigation",
                "skill_id": "linux.navigation",
                "mission_id": "emergency.lighting.01",
                "assisted": True,
                "delta": 1,
            }
        ],
        "rewards": [{"reward_id": "lore.emergency.lighting.01"}],
    }
    first = client.post("/v1/progress/sync", json=body, headers=headers)
    assert first.status_code == 200
    payload = first.json()
    assert payload["clearance_points"] == 25
    assert payload["rank"] == "Emergency Operator"
    assert payload["skills"][0]["skill_id"] == "linux.navigation"
    assert payload["skills"][0]["mastery_level"] == "introduced"
    assert payload["skills"][0]["unassisted_evidence_count"] == 0
    assert payload["rewards"][0]["reward_id"] == "lore.emergency.lighting.01"

    # Replay with unassisted completion: least-assisted wins, no double clearance.
    headers2 = {
        "Authorization": f"Bearer {token}",
        "Idempotency-Key": "prog-sync-2",
    }
    body2 = {
        "missions": [
            {
                "mission_id": "emergency.lighting.01",
                "status": "completed",
                "best_hint_level": 0,
                "clearance_awarded": 25,
                "attempt_count": 2,
                "mission_version": "1.0.0",
            }
        ],
        "skill_evidence": [
            {
                "event_id": "emergency.lighting.01:linux.navigation",
                "skill_id": "linux.navigation",
                "mission_id": "emergency.lighting.01",
                "assisted": False,
                "delta": 1,
            }
        ],
        "rewards": [{"reward_id": "lore.emergency.lighting.01"}],
    }
    second = client.post("/v1/progress/sync", json=body2, headers=headers2)
    assert second.status_code == 200
    again = second.json()
    assert again["clearance_points"] == 25
    assert again["missions"][0]["best_hint_level"] == 0
    assert again["missions"][0]["attempt_count"] == 2
    assert len(again["skills"]) == 1
    assert len(again["rewards"]) == 1

    listed = client.get("/v1/progress", headers={"Authorization": f"Bearer {token}"})
    assert listed.status_code == 200
    assert listed.json()["clearance_points"] == 25
    assert listed.json()["review_recommendations"]
