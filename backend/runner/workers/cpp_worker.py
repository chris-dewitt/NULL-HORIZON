"""Isolated C++ worker for trusted development."""

from __future__ import annotations

import os
import shutil
import subprocess
import time
from pathlib import Path

from runner.contracts.models import ExecutionRequest, ExecutionResult
from runner.limits import make_preexec
from runner.normalize import normalize_cpp_output
from runner.workspace import WorkspaceManager


class CppWorker:
    def __init__(self, workspaces: WorkspaceManager | None = None) -> None:
        self.workspaces = workspaces or WorkspaceManager()

    def run(
        self,
        request: ExecutionRequest,
        *,
        cancelled: bool = False,
    ) -> ExecutionResult:
        if cancelled:
            return normalize_cpp_output(
                execution_id=request.execution_id,
                compile_returncode=1,
                run_returncode=None,
                stdout="",
                stderr="cancelled",
                max_output_bytes=request.limits.max_output_bytes,
                cancelled=True,
            )

        compiler = shutil.which("c++") or shutil.which("g++")
        if compiler is None:
            return normalize_cpp_output(
                execution_id=request.execution_id,
                compile_returncode=1,
                run_returncode=None,
                stdout="",
                stderr="C++ compiler not available on this host",
                max_output_bytes=request.limits.max_output_bytes,
            )

        workspace = self.workspaces.create(request.execution_id, request.files)
        source = _find_source(workspace)
        binary = workspace / "a.out"
        started = time.monotonic()
        timed_out = False
        try:
            compile_proc = subprocess.run(  # noqa: S603
                [compiler, "-std=c++17", "-O0", "-o", str(binary), str(source)],
                cwd=str(workspace),
                capture_output=True,
                text=True,
                timeout=request.limits.wall_timeout_sec,
                check=False,
                preexec_fn=make_preexec(request.limits),
            )
        except subprocess.TimeoutExpired as exc:
            stdout = (
                exc.stdout.decode("utf-8", errors="ignore")
                if isinstance(exc.stdout, bytes)
                else (exc.stdout or "")
            )
            stderr = (
                exc.stderr.decode("utf-8", errors="ignore")
                if isinstance(exc.stderr, bytes)
                else (exc.stderr or "")
            )
            return normalize_cpp_output(
                execution_id=request.execution_id,
                compile_returncode=124,
                run_returncode=None,
                stdout=stdout,
                stderr=stderr,
                max_output_bytes=request.limits.max_output_bytes,
                timed_out=True,
                metrics={"runtime": "cpp", "phase": "compile"},
            )

        if compile_proc.returncode != 0:
            return normalize_cpp_output(
                execution_id=request.execution_id,
                compile_returncode=compile_proc.returncode,
                run_returncode=None,
                stdout=compile_proc.stdout,
                stderr=compile_proc.stderr,
                max_output_bytes=request.limits.max_output_bytes,
                metrics={"runtime": "cpp", "phase": "compile"},
            )

        try:
            run_proc = subprocess.run(  # noqa: S603
                [str(binary)],
                cwd=str(workspace),
                capture_output=True,
                text=True,
                timeout=request.limits.wall_timeout_sec,
                check=False,
                preexec_fn=make_preexec(request.limits),
                env={**os.environ, "http_proxy": "http://127.0.0.1:9"},
            )
            run_code = run_proc.returncode
            stdout = (compile_proc.stdout or "") + (run_proc.stdout or "")
            stderr = (compile_proc.stderr or "") + (run_proc.stderr or "")
        except subprocess.TimeoutExpired as exc:
            timed_out = True
            run_code = 124
            stdout = (
                exc.stdout.decode("utf-8", errors="ignore")
                if isinstance(exc.stdout, bytes)
                else (exc.stdout or "")
            )
            stderr = (
                exc.stderr.decode("utf-8", errors="ignore")
                if isinstance(exc.stderr, bytes)
                else (exc.stderr or "")
            )

        duration_ms = int((time.monotonic() - started) * 1000)
        return normalize_cpp_output(
            execution_id=request.execution_id,
            compile_returncode=0,
            run_returncode=run_code,
            stdout=stdout,
            stderr=stderr,
            max_output_bytes=request.limits.max_output_bytes,
            timed_out=timed_out,
            metrics={"run_ms": duration_ms, "runtime": "cpp"},
        )


def _find_source(workspace: Path) -> Path:
    for name in ("main.cpp", "solution.cpp", "program.cpp"):
        candidate = workspace / name
        if candidate.exists():
            return candidate
    cpp_files = sorted(workspace.rglob("*.cpp"))
    if not cpp_files:
        raise FileNotFoundError("No .cpp source file in workspace")
    return cpp_files[0]
