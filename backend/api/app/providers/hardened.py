"""Blocked production sandbox adapter until security review."""

from __future__ import annotations

from runner.contracts.provider import DeploymentBlockedError

from api.app.schemas import ExecutionResultResponse, ExecutionSubmitRequest


class HardenedBlockedApiAdapter:
    """Public hardened execution remains disabled (ADR-0011)."""

    def build_result(
        self,
        execution_id: str,
        request: ExecutionSubmitRequest,
    ) -> ExecutionResultResponse:
        raise DeploymentBlockedError(
            "Public hardened sandbox is blocked until security review "
            "(PUBLIC_EXECUTION_ENABLED=false)."
        )
