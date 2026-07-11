"""Orchestrator queue, cancel, and retention tests."""

from __future__ import annotations

import os
import time
from pathlib import Path

from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.orchestrator import ExecutionOrchestrator
from runner.workspace import WorkspaceManager


def _request(
    execution_id: str,
    *,
    content: str = "def test_ok():\n    assert True\n",
    timeout: float = 5.0,
) -> ExecutionRequest:
    return ExecutionRequest(
        execution_id=execution_id,
        mission_id="m",
        mission_version="1.0.0",
        runtime="python",
        environment_id="python.trusted.v1",
        files=[ExecutionFile(path="test_ok.py", content=content)],
        limits=ExecutionLimits(wall_timeout_sec=timeout, max_output_bytes=8_000),
    )


def test_cancel_before_drain(tmp_path: Path) -> None:
    orch = ExecutionOrchestrator(
        workspaces=WorkspaceManager(root=tmp_path),
        auto_run=False,
    )
    orch.submit(_request("cancel-me"))
    orch.cancel("cancel-me")
    result = orch.get_result("cancel-me")
    assert result.outcome == "cancelled"
    assert orch.drain() == 0


def test_submit_runs_python_job(tmp_path: Path) -> None:
    orch = ExecutionOrchestrator(
        workspaces=WorkspaceManager(root=tmp_path),
        auto_run=True,
    )
    receipt = orch.submit(_request("pass-me"))
    assert receipt.execution_id == "pass-me"
    result = orch.get_result("pass-me")
    assert result.status == "completed"
    assert result.outcome == "passed"
    assert not orch.workspaces.path_for("pass-me").exists()


def test_cleanup_expired_drops_queue_state(tmp_path: Path) -> None:
    orch = ExecutionOrchestrator(
        workspaces=WorkspaceManager(root=tmp_path),
        auto_run=False,
    )
    # Leave an orphan workspace as if a crash happened mid-job.
    path = orch.workspaces.create(
        "stale",
        [ExecutionFile(path="x.txt", content="1")],
    )
    old = time.time() - 9_999
    os.utime(path, (old, old))
    removed = orch.cleanup_expired(retention_sec=1)
    assert "stale" in removed
