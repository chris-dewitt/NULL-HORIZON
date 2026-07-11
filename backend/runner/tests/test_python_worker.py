"""Python worker isolation, timeout, network, and output tests."""

from __future__ import annotations

from pathlib import Path

from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.workers.python_worker import PythonWorker
from runner.workspace import WorkspaceManager


def _run(
    tmp_path: Path,
    *,
    execution_id: str,
    files: list[ExecutionFile],
    limits: ExecutionLimits | None = None,
):
    worker = PythonWorker(WorkspaceManager(root=tmp_path / "ws"))
    request = ExecutionRequest(
        execution_id=execution_id,
        mission_id="m",
        mission_version="1.0.0",
        runtime="python",
        environment_id="python.trusted.v1",
        files=files,
        limits=limits or ExecutionLimits(wall_timeout_sec=5.0, max_output_bytes=4_000),
    )
    return worker.run(request)


def test_python_timeout(tmp_path: Path) -> None:
    result = _run(
        tmp_path,
        execution_id="timeout",
        files=[
            ExecutionFile(
                path="test_sleep.py",
                content=("import time\ndef test_sleep():\n    time.sleep(30)\n"),
            )
        ],
        limits=ExecutionLimits(wall_timeout_sec=1.0, max_output_bytes=2_000),
    )
    assert result.outcome == "timeout"


def test_python_network_blocked(tmp_path: Path) -> None:
    result = _run(
        tmp_path,
        execution_id="net",
        files=[
            ExecutionFile(
                path="test_net.py",
                content=(
                    "import socket\n"
                    "def test_network_blocked():\n"
                    "    s = socket.socket()\n"
                    "    try:\n"
                    "        s.connect(('1.1.1.1', 80))\n"
                    "        assert False, 'connect should be blocked'\n"
                    "    except OSError as exc:\n"
                    "        assert 'Network disabled' in str(exc)\n"
                    "    finally:\n"
                    "        s.close()\n"
                ),
            )
        ],
    )
    assert result.outcome == "passed"


def test_python_output_truncated(tmp_path: Path) -> None:
    result = _run(
        tmp_path,
        execution_id="trunc",
        files=[
            ExecutionFile(
                path="test_big.py",
                content=("def test_big():\n    print('X' * 50_000)\n    assert True\n"),
            )
        ],
        limits=ExecutionLimits(wall_timeout_sec=5.0, max_output_bytes=500),
    )
    assert result.truncated is True
    assert "[truncated]" in result.stdout


def test_jobs_cannot_see_finished_sibling_workspace(tmp_path: Path) -> None:
    """After a job finishes, its workspace is destroyed so peers cannot read it."""
    root = tmp_path / "ws"
    orch_workspaces = WorkspaceManager(root=root)
    from runner.orchestrator import ExecutionOrchestrator

    orch = ExecutionOrchestrator(workspaces=orch_workspaces, auto_run=True)
    first = orch.submit(
        ExecutionRequest(
            execution_id="job-a",
            mission_id="m",
            mission_version="1.0.0",
            runtime="python",
            environment_id="python.trusted.v1",
            files=[
                ExecutionFile(
                    path="test_ok.py",
                    content="def test_ok():\n    assert True\n",
                ),
                ExecutionFile(path="secret.txt", content="TOP-SECRET"),
            ],
            limits=ExecutionLimits(wall_timeout_sec=5.0),
        )
    )
    assert first.execution_id == "job-a"
    assert not orch_workspaces.path_for("job-a").exists()

    worker = PythonWorker(orch_workspaces)
    result = worker.run(
        ExecutionRequest(
            execution_id="job-b",
            mission_id="m",
            mission_version="1.0.0",
            runtime="python",
            environment_id="python.trusted.v1",
            files=[
                ExecutionFile(
                    path="test_iso.py",
                    content=(
                        "from pathlib import Path\n"
                        "def test_no_sibling():\n"
                        "    secret = Path('..') / 'job-a' / 'secret.txt'\n"
                        "    assert not secret.exists()\n"
                    ),
                )
            ],
            limits=ExecutionLimits(wall_timeout_sec=5.0),
        )
    )
    assert result.outcome == "passed"
