"""Execution provider contracts. Learner code never runs in the API process."""

from __future__ import annotations

from typing import Protocol

from api.app.schemas import ExecutionResultResponse, ExecutionSubmitRequest


class ExecutionProvider(Protocol):
    def build_result(
        self,
        execution_id: str,
        request: ExecutionSubmitRequest,
    ) -> ExecutionResultResponse: ...
