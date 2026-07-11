#!/usr/bin/env python3
"""Validate NULL HORIZON content YAML against JSON Schema and referential rules."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

import yaml
from jsonschema import Draft202012Validator
from referencing import Registry, Resource

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "content"
SCHEMA_DIR = CONTENT / "schema"

KIND_DIRS = {
    "skill": CONTENT / "skills",
    "chapter": CONTENT / "chapters",
    "mission": CONTENT / "missions",
    "dialogue": CONTENT / "dialogue",
    "reward": CONTENT / "rewards",
}

ID_FIELDS = {
    "skill": "skill_id",
    "chapter": "chapter_id",
    "mission": "mission_id",
    "dialogue": "dialogue_id",
    "reward": "reward_id",
}


def load_yaml(path: Path) -> dict[str, Any]:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: expected a mapping document")
    return data


def load_schema(name: str) -> dict[str, Any]:
    schema_path = SCHEMA_DIR / f"{name}.schema.json"
    return json.loads(schema_path.read_text(encoding="utf-8"))


def build_registry() -> Registry:
    resources: list[tuple[str, Resource[Any]]] = []
    for path in SCHEMA_DIR.glob("*.schema.json"):
        schema = json.loads(path.read_text(encoding="utf-8"))
        resource = Resource.from_contents(schema)
        resources.append((path.name, resource))
        if "$id" in schema:
            resources.append((schema["$id"], resource))
    registry: Registry = Registry()
    for uri, resource in resources:
        registry = registry.with_resource(uri, resource)
    return registry


def collect_documents() -> dict[str, list[tuple[Path, dict[str, Any]]]]:
    docs: dict[str, list[tuple[Path, dict[str, Any]]]] = {}
    for kind, directory in KIND_DIRS.items():
        docs[kind] = []
        if not directory.exists():
            continue
        for path in sorted(directory.glob("*.yaml")):
            docs[kind].append((path, load_yaml(path)))
    return docs


def schema_errors(kind: str, path: Path, document: dict[str, Any], registry: Registry) -> list[str]:
    schema_name = kind
    schema = load_schema(schema_name)
    validator = Draft202012Validator(schema, registry=registry)
    return [
        f"{path}: {error.message}"
        for error in sorted(validator.iter_errors(document), key=lambda err: err.json_path)
    ]


def referential_errors(docs: dict[str, list[tuple[Path, dict[str, Any]]]]) -> list[str]:
    errors: list[str] = []
    ids: dict[str, set[str]] = {}
    for kind, items in docs.items():
        field = ID_FIELDS[kind]
        seen: set[str] = set()
        for path, document in items:
            value = document[field]
            if value in seen:
                errors.append(f"{path}: duplicate {field} '{value}'")
            seen.add(value)
        ids[kind] = seen

    skill_ids = ids.get("skill", set())
    dialogue_ids = ids.get("dialogue", set())
    mission_ids = ids.get("mission", set())
    reward_ids = ids.get("reward", set())

    for path, chapter in docs.get("chapter", []):
        for mission_id in chapter.get("mission_ids", []):
            if mission_id not in mission_ids:
                errors.append(f"{path}: chapter references missing mission '{mission_id}'")

    for path, skill in docs.get("skill", []):
        for prereq in skill.get("prerequisites", []):
            if prereq not in skill_ids:
                errors.append(f"{path}: skill prerequisite '{prereq}' does not exist")

    for path, mission in docs.get("mission", []):
        primary = mission["skills"]["primary"]
        if primary not in skill_ids:
            errors.append(f"{path}: primary skill '{primary}' does not exist")
        for secondary in mission["skills"].get("secondary", []):
            if secondary not in skill_ids:
                errors.append(f"{path}: secondary skill '{secondary}' does not exist")
        for prereq in mission["requirements"].get("prerequisite_skills", []):
            if prereq not in skill_ids:
                errors.append(f"{path}: prerequisite skill '{prereq}' does not exist")

        briefing = mission["narrative"]["briefing_dialogue_id"]
        success = mission["narrative"]["success_dialogue_id"]
        if briefing not in dialogue_ids:
            errors.append(f"{path}: briefing dialogue '{briefing}' does not exist")
        if success not in dialogue_ids:
            errors.append(f"{path}: success dialogue '{success}' does not exist")

        objective_ids = [objective["id"] for objective in mission["objectives"]]
        if len(objective_ids) != len(set(objective_ids)):
            errors.append(f"{path}: duplicate objective ids")

        for objective in mission["objectives"]:
            if objective["type"] == "state_assertion" and "assert" not in objective:
                errors.append(f"{path}: state_assertion '{objective['id']}' lacks assert")
            if objective.get("visible") is False and "assert" in objective:
                description = objective["description"].lower()
                for value in objective["assert"].values():
                    if str(value).lower() in description:
                        errors.append(
                            f"{path}: hidden objective '{objective['id']}' may leak assert value"
                        )

        hint_levels = [hint["level"] for hint in mission.get("hints", [])]
        if hint_levels and hint_levels != list(range(1, len(hint_levels) + 1)):
            errors.append(f"{path}: hint levels must be sequential starting at 1")

        if mission["requirements"]["online"] and not mission["environment"].get("template_id"):
            errors.append(f"{path}: online mission lacks execution environment template")

        for objective_id in mission["completion"]["objective_ids"]:
            if objective_id not in objective_ids:
                errors.append(f"{path}: completion references missing objective '{objective_id}'")

        for unlock in mission["rewards"].get("unlocks", []):
            if unlock not in reward_ids:
                errors.append(f"{path}: reward unlock '{unlock}' does not exist")

        for file_entry in mission["environment"].get("files", []):
            target = file_entry.get("target", "")
            if ".." in target or not target.startswith("/workspace/"):
                errors.append(f"{path}: editable path escapes workspace: {target}")

        filesystem = mission["environment"].get("filesystem")
        if filesystem:
            cwd = filesystem.get("cwd", "")
            if ".." in cwd or not cwd.startswith("/"):
                errors.append(f"{path}: filesystem cwd must be an absolute sandbox path")
            for entry in filesystem.get("entries", []):
                entry_path = entry.get("path", "")
                if ".." in entry_path.split("/") or not entry_path.startswith("/"):
                    errors.append(f"{path}: filesystem entry escapes sandbox: {entry_path}")
            if mission["requirements"]["online"] is False and "terminal" in mission.get("tools", []):
                if filesystem.get("entries") is None:
                    errors.append(f"{path}: terminal mission lacks filesystem entries")

    return errors


def validate() -> int:
    docs = collect_documents()
    registry = build_registry()
    errors: list[str] = []
    for kind, items in docs.items():
        for path, document in items:
            errors.extend(schema_errors(kind, path, document, registry))
    errors.extend(referential_errors(docs))
    if errors:
        print("Content validation failed:", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1
    total = sum(len(items) for items in docs.values())
    print(f"Content validation passed ({total} documents)")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    return validate()


if __name__ == "__main__":
    sys.exit(main())
