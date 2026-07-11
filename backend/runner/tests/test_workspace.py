"""Workspace isolation and retention tests."""

from __future__ import annotations

import time
from pathlib import Path

import pytest
from runner.contracts.models import ExecutionFile
from runner.workspace import WorkspaceManager


def test_workspaces_are_isolated(tmp_path: Path) -> None:
    mgr = WorkspaceManager(root=tmp_path)
    a = mgr.create(
        "job-a",
        [ExecutionFile(path="secret.txt", content="alpha-secret")],
    )
    b = mgr.create(
        "job-b",
        [ExecutionFile(path="secret.txt", content="beta-secret")],
    )
    assert (a / "secret.txt").read_text(encoding="utf-8") == "alpha-secret"
    assert (b / "secret.txt").read_text(encoding="utf-8") == "beta-secret"
    # Job B must not contain job A's files under its tree.
    assert list(b.iterdir()) == [b / "secret.txt"]
    assert "alpha-secret" not in (b / "secret.txt").read_text(encoding="utf-8")
    assert a.resolve() != b.resolve()


def test_path_escape_rejected(tmp_path: Path) -> None:
    mgr = WorkspaceManager(root=tmp_path)
    with pytest.raises(ValueError, match="Illegal workspace path"):
        mgr.create(
            "evil",
            [ExecutionFile(path="../outside.txt", content="nope")],
        )


def test_retention_cleanup(tmp_path: Path) -> None:
    mgr = WorkspaceManager(root=tmp_path)
    mgr.create("old-job", [ExecutionFile(path="a.txt", content="x")])
    path = mgr.path_for("old-job")
    # Backdate mtime so retention treats it as expired.
    old = time.time() - 10_000
    import os

    os.utime(path, (old, old))
    removed = mgr.cleanup_expired(retention_sec=1)
    assert "old-job" in removed
    assert not path.exists()
