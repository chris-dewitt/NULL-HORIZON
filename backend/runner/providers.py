"""Runner-side providers used by the API adapter layer."""

from __future__ import annotations

from runner.contracts.models import ExecutionReceipt, ExecutionRequest, ExecutionResult
from runner.contracts.provider import DeploymentBlockedError, ExecutionProvider
from runner.orchestrator import ExecutionOrchestrator


class LocalTrustedProvider:
    """Trusted-development provider that executes via isolated worker subprocesses."""

    def __init__(self, orchestrator: ExecutionOrchestrator | None = None) -> None:
        self.orchestrator = orchestrator or ExecutionOrchestrator(auto_run=True)

    def submit(self, request: ExecutionRequest) -> ExecutionReceipt:
        return self.orchestrator.submit(request)

    def get_result(self, execution_id: str) -> ExecutionResult:
        return self.orchestrator.get_result(execution_id)

    def cancel(self, execution_id: str) -> None:
        self.orchestrator.cancel(execution_id)


class HardenedSandboxProvider:
    """Production provider placeholder — blocked until security review."""

    def submit(self, request: ExecutionRequest) -> ExecutionReceipt:
        raise DeploymentBlockedError(
            "Public hardened sandbox is blocked until security review "
            "(PUBLIC_EXECUTION_ENABLED=false)."
        )

    def get_result(self, execution_id: str) -> ExecutionResult:
        raise DeploymentBlockedError("Public hardened sandbox is blocked")

    def cancel(self, execution_id: str) -> None:
        raise DeploymentBlockedError("Public hardened sandbox is blocked")


def build_provider(name: str) -> ExecutionProvider:
    if name == "local_trusted":
        return LocalTrustedProvider()
    if name in {"hardened", "production"}:
        return HardenedSandboxProvider()
    raise ValueError(f"Unknown runner provider: {name}")
