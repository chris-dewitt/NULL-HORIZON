"""Tests for content validation and bundle build."""

from __future__ import annotations

import json
import sys
from pathlib import Path

import pytest
import yaml

ROOT = Path(__file__).resolve().parents[2]
SCRIPTS = ROOT / "scripts"
sys.path.insert(0, str(SCRIPTS))

from build_bundle import build_bundle  # noqa: E402
from validate_content import validate  # noqa: E402


def test_validate_current_content_passes() -> None:
    assert validate() == 0


def test_invalid_mission_fails(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> None:
    import validate_content as vc

    skills = tmp_path / "skills"
    chapters = tmp_path / "chapters"
    missions = tmp_path / "missions"
    dialogue = tmp_path / "dialogue"
    rewards = tmp_path / "rewards"
    for path in (skills, chapters, missions, dialogue, rewards):
        path.mkdir()

    (skills / "s.yaml").write_text(
        yaml.safe_dump(
            {
                "schema_version": 1,
                "skill_id": "computational_thinking.observe",
                "name": "Observe",
                "domain": "computational_thinking",
            }
        ),
        encoding="utf-8",
    )
    (dialogue / "d.yaml").write_text(
        yaml.safe_dump(
            {
                "schema_version": 1,
                "dialogue_id": "dialogue.emergency.lighting.briefing",
                "lines": [{"speaker": "ORION", "text": "Hello"}],
            }
        ),
        encoding="utf-8",
    )
    (dialogue / "d2.yaml").write_text(
        yaml.safe_dump(
            {
                "schema_version": 1,
                "dialogue_id": "dialogue.emergency.lighting.success",
                "lines": [{"speaker": "ORION", "text": "Done"}],
            }
        ),
        encoding="utf-8",
    )
    (missions / "bad.yaml").write_text(
        yaml.safe_dump(
            {
                "schema_version": 1,
                "mission_id": "emergency.lighting.01",
                "version": "1.0.0",
                "chapter_id": "emergency_interface",
                "title": "Broken",
                "summary": "Missing primary skill on purpose? actually present",
                "difficulty": "introductory",
                "requirements": {"app_version": ">=0.1.0", "online": False},
                "skills": {"primary": "missing.skill"},
                "narrative": {
                    "briefing_dialogue_id": "dialogue.emergency.lighting.briefing",
                    "success_dialogue_id": "dialogue.emergency.lighting.success",
                },
                "tools": ["systems_panel"],
                "environment": {
                    "schema_version": 1,
                    "template_id": "local.state.v1",
                    "seed": 1,
                    "initial_state": {"a": 1},
                },
                "objectives": [
                    {
                        "id": "o1",
                        "type": "state_assertion",
                        "description": "Do thing",
                        "visible": True,
                        "assert": {"a": 1},
                    }
                ],
                "hints": [{"level": 1, "text": "hint"}],
                "rewards": {"clearance_points": 1},
                "completion": {"mode": "all", "objective_ids": ["o1"]},
            }
        ),
        encoding="utf-8",
    )
    (chapters / "c.yaml").write_text(
        yaml.safe_dump(
            {
                "schema_version": 1,
                "chapter_id": "emergency_interface",
                "title": "Emergency Interface",
                "region": "Emergency Interface",
                "mission_ids": ["emergency.lighting.01"],
            }
        ),
        encoding="utf-8",
    )

    monkeypatch.setattr(vc, "CONTENT", tmp_path)
    monkeypatch.setattr(
        vc,
        "KIND_DIRS",
        {
            "skill": skills,
            "chapter": chapters,
            "mission": missions,
            "dialogue": dialogue,
            "reward": rewards,
        },
    )
    # Keep real schemas
    assert vc.validate() == 1


def test_build_bundle_writes_manifest_and_checksums(tmp_path: Path) -> None:
    bundle_dir = build_bundle("dev", tmp_path)
    manifest = json.loads((bundle_dir / "manifest.json").read_text(encoding="utf-8"))
    checksums = json.loads((bundle_dir / "checksums.json").read_text(encoding="utf-8"))
    assert manifest["bundle_id"] == "null-horizon-starter"
    assert "emergency.lighting.01" in manifest["missions"]
    assert "manifest.json" in checksums["files"]
    mission = json.loads(
        (bundle_dir / "missions" / "emergency.lighting.01.json").read_text(encoding="utf-8")
    )
    assert mission["title"] == "Emergency Lighting"
    assert mission["environment"]["seed"] == 42
