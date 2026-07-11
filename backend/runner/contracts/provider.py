"""Provider protocol for execution backends."""

from __future__ import annotations

from typing import Protocol

from runner.contracts.models import ExecutionReceipt, ExecutionRequest, ExecutionResult


class ExecutionProvider(Protocol):
    def submit(self, request: ExecutionRequest) -> ExecutionReceipt: ...

    def get_result(self, execution_id: str) -> ExecutionResult: ...

    def cancel(self, execution_id: str) -> None: ...


class DeploymentBlockedError(RuntimeError):
    """Raised when public hardened execution is not yet approved."""
