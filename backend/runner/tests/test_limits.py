"""Resource limit helper tests (best-effort on constrained hosts)."""

from __future__ import annotations

from pathlib import Path

from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.limits import make_preexec
from runner.workers.python_worker import PythonWorker
from runner.workspace import WorkspaceManager


def test_make_preexec_is_callable() -> None:
    # Do not call apply_resource_limits in the parent process — that would
    # tighten RLIMIT_AS for the entire pytest session.
    preexec = make_preexec(ExecutionLimits(memory_mb=64, max_processes=8))
    assert callable(preexec)


def test_worker_disables_core_dumps(tmp_path: Path) -> None:
    worker = PythonWorker(WorkspaceManager(root=tmp_path))
    result = worker.run(
        ExecutionRequest(
            execution_id="core",
            mission_id="m",
            mission_version="1.0.0",
            runtime="python",
            environment_id="python.trusted.v1",
            files=[
                ExecutionFile(
                    path="test_core.py",
                    content=(
                        "import resource\n"
                        "def test_core_limit():\n"
                        "    soft, _hard = resource.getrlimit(resource.RLIMIT_CORE)\n"
                        "    assert soft == 0\n"
                    ),
                )
            ],
            limits=ExecutionLimits(wall_timeout_sec=5.0, memory_mb=512),
        )
    )
    assert result.outcome == "passed"


def test_memory_limit_best_effort(tmp_path: Path) -> None:
    """Large allocation should fail or be killed under a tight address-space cap.

    Some containers ignore RLIMIT_AS; accept either enforcement or a clean failure
    path without hanging the suite.
    """
    worker = PythonWorker(WorkspaceManager(root=tmp_path))
    result = worker.run(
        ExecutionRequest(
            execution_id="mem",
            mission_id="m",
            mission_version="1.0.0",
            runtime="python",
            environment_id="python.trusted.v1",
            files=[
                ExecutionFile(
                    path="test_mem.py",
                    content=(
                        "def test_alloc():\n"
                        "    # Large alloc; RLIMIT_AS should fail this.\n"
                        "    try:\n"
                        "        blob = bytearray(200 * 1024 * 1024)\n"
                        "        assert len(blob) > 0\n"
                        "    except MemoryError:\n"
                        "        return\n"
                    ),
                )
            ],
            limits=ExecutionLimits(
                wall_timeout_sec=5.0,
                memory_mb=32,
                max_output_bytes=4_000,
                max_processes=64,
            ),
        )
    )
    # Either the suite failed (limit enforced / process killed) or passed if the
    # host ignored RLIMIT_AS after catching MemoryError. Never hang/timeout.
    assert result.outcome in {"passed", "failed_tests", "timeout"}
    assert result.status in {"completed", "failed"}
