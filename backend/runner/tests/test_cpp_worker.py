"""C++ worker smoke tests (skipped when no compiler)."""

from __future__ import annotations

import shutil
from pathlib import Path

import pytest
from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.workers.cpp_worker import CppWorker
from runner.workspace import WorkspaceManager

pytestmark = pytest.mark.skipif(
    shutil.which("c++") is None and shutil.which("g++") is None,
    reason="C++ compiler not available",
)


def test_cpp_pass(tmp_path: Path) -> None:
    worker = CppWorker(WorkspaceManager(root=tmp_path))
    result = worker.run(
        ExecutionRequest(
            execution_id="cpp1",
            mission_id="m",
            mission_version="1.0.0",
            runtime="cpp",
            environment_id="cpp.trusted.v1",
            files=[
                ExecutionFile(
                    path="main.cpp",
                    content="int main() { return 0; }\n",
                )
            ],
            limits=ExecutionLimits(wall_timeout_sec=10.0),
        )
    )
    assert result.outcome == "passed"


def test_cpp_compile_error(tmp_path: Path) -> None:
    worker = CppWorker(WorkspaceManager(root=tmp_path))
    result = worker.run(
        ExecutionRequest(
            execution_id="cpp-bad",
            mission_id="m",
            mission_version="1.0.0",
            runtime="cpp",
            environment_id="cpp.trusted.v1",
            files=[
                ExecutionFile(
                    path="main.cpp",
                    content="not valid c++ {{{{\n",
                )
            ],
            limits=ExecutionLimits(wall_timeout_sec=10.0),
        )
    )
    assert result.outcome == "compile_error"
