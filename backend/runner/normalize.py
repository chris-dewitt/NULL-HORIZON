"""Normalize raw worker output into ExecutionResult."""

from __future__ import annotations

import re
from typing import Any

from runner.contracts.models import ExecutionResult, TestCaseResult

_PYTEST_LINE = re.compile(
    r"^(?P<name>[\w./:\[\]\-]+)\s+(?P<status>PASSED|FAILED|SKIPPED|ERROR)"
)


def truncate_output(text: str, max_bytes: int) -> tuple[str, bool]:
    raw = text.encode("utf-8", errors="replace")
    if len(raw) <= max_bytes:
        return text, False
    clipped = raw[:max_bytes].decode("utf-8", errors="ignore")
    return clipped + "\n[truncated]\n", True


def normalize_pytest_output(
    *,
    execution_id: str,
    returncode: int,
    stdout: str,
    stderr: str,
    max_output_bytes: int,
    timed_out: bool = False,
    cancelled: bool = False,
    metrics: dict[str, Any] | None = None,
) -> ExecutionResult:
    stdout, out_trunc = truncate_output(stdout, max_output_bytes)
    stderr, err_trunc = truncate_output(stderr, max_output_bytes)
    truncated = out_trunc or err_trunc

    if cancelled:
        return ExecutionResult(
            execution_id=execution_id,
            status="cancelled",
            outcome="cancelled",
            stdout=stdout,
            stderr=stderr,
            truncated=truncated,
            metrics=metrics or {},
        )
    if timed_out:
        return ExecutionResult(
            execution_id=execution_id,
            status="failed",
            outcome="timeout",
            stdout=stdout,
            stderr=(stderr + "\nExecution timed out.").strip(),
            truncated=truncated,
            metrics=metrics or {},
        )

    tests = _parse_pytest_lines(stdout + "\n" + stderr)
    if not tests and returncode == 0:
        tests = [TestCaseResult(id="suite", status="passed")]
    elif not tests and returncode != 0:
        tests = [
            TestCaseResult(
                id="suite",
                status="failed",
                message="Worker exited non-zero without parsed test lines",
            )
        ]

    all_passed = bool(tests) and all(t.status == "passed" for t in tests)
    outcome = "passed" if all_passed and returncode == 0 else "failed_tests"
    return ExecutionResult(
        execution_id=execution_id,
        status="completed",
        outcome=outcome,
        stdout=stdout,
        stderr=stderr,
        tests=tests,
        truncated=truncated,
        metrics=metrics or {},
    )


def normalize_cpp_output(
    *,
    execution_id: str,
    compile_returncode: int,
    run_returncode: int | None,
    stdout: str,
    stderr: str,
    max_output_bytes: int,
    timed_out: bool = False,
    cancelled: bool = False,
    metrics: dict[str, Any] | None = None,
) -> ExecutionResult:
    stdout, out_trunc = truncate_output(stdout, max_output_bytes)
    stderr, err_trunc = truncate_output(stderr, max_output_bytes)
    truncated = out_trunc or err_trunc
    if cancelled:
        return ExecutionResult(
            execution_id=execution_id,
            status="cancelled",
            outcome="cancelled",
            stdout=stdout,
            stderr=stderr,
            truncated=truncated,
            metrics=metrics or {},
        )
    if timed_out:
        return ExecutionResult(
            execution_id=execution_id,
            status="failed",
            outcome="timeout",
            stdout=stdout,
            stderr=(stderr + "\nExecution timed out.").strip(),
            truncated=truncated,
            metrics=metrics or {},
        )
    if compile_returncode != 0:
        return ExecutionResult(
            execution_id=execution_id,
            status="completed",
            outcome="compile_error",
            stdout=stdout,
            stderr=stderr,
            tests=[
                TestCaseResult(
                    id="compile",
                    status="failed",
                    message="Compile failed",
                )
            ],
            truncated=truncated,
            metrics=metrics or {},
        )
    passed = run_returncode == 0
    return ExecutionResult(
        execution_id=execution_id,
        status="completed",
        outcome="passed" if passed else "failed_tests",
        stdout=stdout,
        stderr=stderr,
        tests=[
            TestCaseResult(
                id="run",
                status="passed" if passed else "failed",
                message=None if passed else f"exit={run_returncode}",
            )
        ],
        truncated=truncated,
        metrics=metrics or {},
    )


def _parse_pytest_lines(text: str) -> list[TestCaseResult]:
    found: list[TestCaseResult] = []
    for line in text.splitlines():
        match = _PYTEST_LINE.search(line.strip())
        if not match:
            continue
        status = match.group("status").lower()
        if status == "error":
            status = "failed"
        found.append(TestCaseResult(id=match.group("name"), status=status))
    return found
