"""Guardrails ensuring learner code stays outside the API process."""

from __future__ import annotations

from pathlib import Path

API_ROOT = Path(__file__).resolve().parents[1]


def test_api_package_has_no_code_exec_helpers() -> None:
    forbidden_tokens = (
        "subprocess.Popen",
        "os.system(",
        "docker.from_env",
        "exec(",
        "eval(",
    )
    offenders: list[str] = []

    for path in API_ROOT.rglob("*.py"):
        if "tests" in path.parts:
            continue
        text = path.read_text(encoding="utf-8")
        for token in forbidden_tokens:
            if token in text:
                offenders.append(f"{path.relative_to(API_ROOT)}:{token}")

    assert offenders == []
