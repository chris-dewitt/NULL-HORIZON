#!/usr/bin/env python3
"""Compile content YAML into a validated JSON bundle with checksums."""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import sys
from pathlib import Path
from typing import Any

import yaml

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "content"
DEFAULT_OUT = CONTENT / "build" / "bundles"


def load_yaml(path: Path) -> dict[str, Any]:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: expected mapping")
    return data


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    digest.update(path.read_bytes())
    return digest.hexdigest()


def compile_kind(source_dir: Path, destination_dir: Path, id_field: str) -> list[str]:
    ids: list[str] = []
    if not source_dir.exists():
        return ids
    for path in sorted(source_dir.glob("*.yaml")):
        document = load_yaml(path)
        entity_id = document[id_field]
        ids.append(entity_id)
        write_json(destination_dir / f"{entity_id}.json", document)
    return ids


def build_bundle(channel: str, out_root: Path) -> Path:
    # Validate first through the sibling script API.
    from validate_content import validate

    code = validate()
    if code != 0:
        raise SystemExit(code)

    bundle_dir = out_root / channel
    if bundle_dir.exists():
        shutil.rmtree(bundle_dir)
    bundle_dir.mkdir(parents=True)

    skills = compile_kind(CONTENT / "skills", bundle_dir / "skills", "skill_id")
    chapters = compile_kind(CONTENT / "chapters", bundle_dir / "chapters", "chapter_id")
    missions = compile_kind(CONTENT / "missions", bundle_dir / "missions", "mission_id")
    dialogues = compile_kind(CONTENT / "dialogue", bundle_dir / "dialogues", "dialogue_id")
    rewards = compile_kind(CONTENT / "rewards", bundle_dir / "rewards", "reward_id")

    manifest = {
        "schema_version": 1,
        "bundle_id": "null-horizon-starter",
        "version": "0.3.0",
        "min_app_version": "0.1.0",
        "content_schema_version": 1,
        "locale": "en",
        "channel": channel,
        "skills": skills,
        "chapters": chapters,
        "missions": missions,
        "dialogues": dialogues,
        "rewards": rewards,
    }
    write_json(bundle_dir / "manifest.json", manifest)

    checksums: dict[str, str] = {}
    for path in sorted(bundle_dir.rglob("*.json")):
        relative = path.relative_to(bundle_dir).as_posix()
        if relative == "checksums.json":
            continue
        checksums[relative] = sha256_file(path)
    write_json(bundle_dir / "checksums.json", {"files": checksums})
    return bundle_dir


def sync_android_assets(bundle_dir: Path) -> None:
    assets = ROOT / "android-app" / "app" / "src" / "main" / "assets" / "content"
    if assets.exists():
        shutil.rmtree(assets)
    shutil.copytree(bundle_dir, assets)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--channel", default="dev", choices=["dev", "staging", "prod"])
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    parser.add_argument(
        "--sync-android-assets",
        action="store_true",
        help="Copy the built bundle into android-app assets/content",
    )
    args = parser.parse_args()
    bundle_dir = build_bundle(args.channel, args.out)
    if args.sync_android_assets:
        sync_android_assets(bundle_dir)
    print(f"Built content bundle at {bundle_dir}")
    return 0


if __name__ == "__main__":
    # Allow importing validate_content from the same directory.
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    sys.exit(main())
