"""Execution orchestrator: queue, workers, cancel, retention."""

from __future__ import annotations

from runner.contracts.models import (
    ExecutionReceipt,
    ExecutionRequest,
    ExecutionResult,
    JobStatus,
)
from runner.queue.memory import MemoryExecutionQueue
from runner.workers.cpp_worker import CppWorker
from runner.workers.python_worker import PythonWorker
from runner.workspace import WorkspaceManager


class ExecutionOrchestrator:
    def __init__(
        self,
        queue: MemoryExecutionQueue | None = None,
        workspaces: WorkspaceManager | None = None,
        auto_run: bool = True,
    ) -> None:
        self.queue = queue or MemoryExecutionQueue()
        self.workspaces = workspaces or WorkspaceManager()
        self.python_worker = PythonWorker(self.workspaces)
        self.cpp_worker = CppWorker(self.workspaces)
        self.auto_run = auto_run

    def submit(self, request: ExecutionRequest) -> ExecutionReceipt:
        self.queue.enqueue(request)
        if self.auto_run:
            self.drain()
            status = self.queue.get_status(request.execution_id) or JobStatus.QUEUED
            return ExecutionReceipt(
                execution_id=request.execution_id,
                status=status.value,
                poll_after_ms=0,
            )
        return ExecutionReceipt(
            execution_id=request.execution_id,
            status=JobStatus.QUEUED.value,
            poll_after_ms=250,
        )

    def drain(self, max_jobs: int = 32) -> int:
        processed = 0
        while processed < max_jobs:
            request = self.queue.dequeue()
            if request is None:
                break
            cancelled = self.queue.is_cancelled(request.execution_id)
            if request.runtime == "cpp":
                result = self.cpp_worker.run(request, cancelled=cancelled)
            else:
                result = self.python_worker.run(request, cancelled=cancelled)
            self.queue.store_result(result)
            # Destroy workspace after capture (retention may keep metadata longer).
            self.workspaces.destroy(request.execution_id)
            processed += 1
        return processed

    def get_result(self, execution_id: str) -> ExecutionResult:
        result = self.queue.get_result(execution_id)
        if result is not None:
            return result
        status = self.queue.get_status(execution_id)
        if status == JobStatus.CANCELLED:
            return ExecutionResult(
                execution_id=execution_id,
                status="cancelled",
                outcome="cancelled",
            )
        return ExecutionResult(
            execution_id=execution_id,
            status=(status or JobStatus.QUEUED).value,
            outcome="pending",
        )

    def cancel(self, execution_id: str) -> None:
        self.queue.mark_cancelled(execution_id)
        # If still queued, synthesize a cancelled result.
        if self.queue.get_result(execution_id) is None:
            self.queue.store_result(
                ExecutionResult(
                    execution_id=execution_id,
                    status="cancelled",
                    outcome="cancelled",
                )
            )
        self.workspaces.destroy(execution_id)

    def cleanup_expired(self, retention_sec: int | None = None) -> list[str]:
        ttl = retention_sec if retention_sec is not None else 3600
        removed = self.workspaces.cleanup_expired(ttl)
        for execution_id in removed:
            self.queue.drop(execution_id)
        return removed
