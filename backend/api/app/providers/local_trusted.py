"""Adapter from API schemas to the trusted-development runner.

Learner code runs only in worker subprocesses owned by ``backend/runner``.
"""

from __future__ import annotations

from typing import Literal, cast

from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.providers import LocalTrustedProvider

from api.app.schemas import (
    ExecutionResultResponse,
    ExecutionSubmitRequest,
    ExecutionTestResult,
)


class LocalTrustedApiAdapter:
    """Enqueues work to the runner orchestrator; does not exec learner source."""

    def __init__(self, provider: LocalTrustedProvider | None = None) -> None:
        self._provider = provider or LocalTrustedProvider()

    def build_result(
        self,
        execution_id: str,
        request: ExecutionSubmitRequest,
    ) -> ExecutionResultResponse:
        limits = ExecutionLimits()
        runner_request = ExecutionRequest(
            execution_id=execution_id,
            mission_id=request.mission_id,
            mission_version=request.mission_version,
            runtime=request.runtime,
            environment_id=request.environment_id,
            files=[
                ExecutionFile(path=item.path, content=item.content)
                for item in request.files
            ],
            limits=limits,
        )
        self._provider.submit(runner_request)
        result = self._provider.get_result(execution_id)
        return ExecutionResultResponse(
            execution_id=result.execution_id,
            status=result.status,
            outcome=result.outcome,
            stdout=result.stdout,
            stderr=result.stderr,
            tests=[
                ExecutionTestResult(
                    id=t.id,
                    status=t.status,
                    message=t.message,
                    visibility=cast(
                        Literal["visible", "hidden"],
                        (
                            t.visibility
                            if t.visibility in {"visible", "hidden"}
                            else "visible"
                        ),
                    ),
                )
                for t in result.tests
            ],
            metrics=result.metrics,
            truncated=result.truncated,
        )
