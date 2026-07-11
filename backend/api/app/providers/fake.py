"""Deterministic fake execution adapter.

Never imports, compiles, or executes learner source. Results are fixture-driven.
"""

from __future__ import annotations

from api.app.schemas import (
    ExecutionResultResponse,
    ExecutionSubmitRequest,
    ExecutionTestResult,
)


class FakeExecutionProvider:
    """Maps workspace file contents to canned test outcomes."""

    def build_result(
        self,
        execution_id: str,
        request: ExecutionSubmitRequest,
    ) -> ExecutionResultResponse:
        joined = "\n".join(f"{item.path}\n{item.content}" for item in request.files)
        if "THRESHOLD = 50" in joined:
            tests = [
                ExecutionTestResult(id="test_safe_valves", status="passed"),
                ExecutionTestResult(id="test_critical_pressure", status="passed"),
            ]
            outcome = "passed"
        elif "THRESHOLD = 30" in joined:
            tests = [
                ExecutionTestResult(
                    id="test_safe_valves",
                    status="failed",
                    message="Expected True for 40 psi",
                ),
                ExecutionTestResult(id="test_critical_pressure", status="passed"),
            ]
            outcome = "failed_tests"
        else:
            tests = [
                ExecutionTestResult(
                    id="test_safe_valves",
                    status="failed",
                    message="Threshold still incorrect",
                ),
            ]
            outcome = "failed_tests"

        return ExecutionResultResponse(
            execution_id=execution_id,
            status="completed",
            outcome=outcome,
            stdout="",
            stderr="",
            tests=tests,
            metrics={"run_ms": 1, "peak_memory_kb": 1024},
            truncated=False,
        )
