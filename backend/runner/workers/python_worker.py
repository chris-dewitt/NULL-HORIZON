"""Isolated Python worker. Runs outside the API request handler trust model."""

from __future__ import annotations

import os
import subprocess
import sys
import time
from pathlib import Path

from runner.contracts.models import ExecutionRequest, ExecutionResult
from runner.limits import make_preexec
from runner.normalize import normalize_pytest_output
from runner.workspace import WorkspaceManager


class PythonWorker:
    def __init__(self, workspaces: WorkspaceManager | None = None) -> None:
        self.workspaces = workspaces or WorkspaceManager()

    def run(
        self,
        request: ExecutionRequest,
        *,
        cancelled: bool = False,
    ) -> ExecutionResult:
        if cancelled:
            return normalize_pytest_output(
                execution_id=request.execution_id,
                returncode=1,
                stdout="",
                stderr="cancelled",
                max_output_bytes=request.limits.max_output_bytes,
                cancelled=True,
            )

        workspace = self.workspaces.create(request.execution_id, request.files)
        # workers/ on PYTHONPATH loads sitecustomize.py → network_guard.install().
        guard_dir = str(Path(__file__).resolve().parent)
        env = os.environ.copy()
        existing = env.get("PYTHONPATH", "")
        env["PYTHONPATH"] = (
            guard_dir if not existing else guard_dir + os.pathsep + existing
        )
        env["PYTHONNOUSERSITE"] = "1"
        # Discourage accidental outbound tooling.
        env["http_proxy"] = "http://127.0.0.1:9"
        env["https_proxy"] = "http://127.0.0.1:9"
        env["NO_PROXY"] = ""
        env["no_proxy"] = ""

        cmd = [
            sys.executable,
            "-m",
            "pytest",
            "-q",
            "-s",
            "--tb=line",
            str(workspace),
        ]
        started = time.monotonic()
        timed_out = False
        try:
            completed = subprocess.run(  # noqa: S603
                cmd,
                cwd=str(workspace),
                env=env,
                capture_output=True,
                text=True,
                timeout=request.limits.wall_timeout_sec,
                check=False,
                preexec_fn=make_preexec(request.limits),
            )
            returncode = completed.returncode
            stdout = completed.stdout
            stderr = completed.stderr
        except subprocess.TimeoutExpired as exc:
            timed_out = True
            returncode = 124
            if isinstance(exc.stdout, bytes):
                stdout = exc.stdout.decode("utf-8", errors="ignore")
            else:
                stdout = exc.stdout or ""
            if isinstance(exc.stderr, bytes):
                stderr = exc.stderr.decode("utf-8", errors="ignore")
            else:
                stderr = exc.stderr or ""
        duration_ms = int((time.monotonic() - started) * 1000)
        return normalize_pytest_output(
            execution_id=request.execution_id,
            returncode=returncode,
            stdout=stdout or "",
            stderr=stderr or "",
            max_output_bytes=request.limits.max_output_bytes,
            timed_out=timed_out,
            metrics={"run_ms": duration_ms, "runtime": "python"},
        )
