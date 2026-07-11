"""In-memory and Redis-backed execution queues."""

from __future__ import annotations

import json
from collections import deque
from dataclasses import asdict
from typing import Any

from runner.contracts.models import ExecutionRequest, ExecutionResult, JobStatus


class MemoryExecutionQueue:
    """Process-local queue suitable for unit tests and single-node trusted dev."""

    def __init__(self) -> None:
        self._pending: deque[str] = deque()
        self._requests: dict[str, ExecutionRequest] = {}
        self._status: dict[str, JobStatus] = {}
        self._results: dict[str, ExecutionResult] = {}
        self._cancelled: set[str] = set()

    def enqueue(self, request: ExecutionRequest) -> None:
        self._requests[request.execution_id] = request
        self._status[request.execution_id] = JobStatus.QUEUED
        self._pending.append(request.execution_id)

    def dequeue(self) -> ExecutionRequest | None:
        while self._pending:
            execution_id = self._pending.popleft()
            if execution_id in self._cancelled:
                self._status[execution_id] = JobStatus.CANCELLED
                continue
            if self._status.get(execution_id) != JobStatus.QUEUED:
                continue
            self._status[execution_id] = JobStatus.RUNNING
            return self._requests[execution_id]
        return None

    def mark_cancelled(self, execution_id: str) -> None:
        self._cancelled.add(execution_id)
        current = self._status.get(execution_id)
        if current in {JobStatus.QUEUED, JobStatus.RUNNING, None}:
            self._status[execution_id] = JobStatus.CANCELLED

    def is_cancelled(self, execution_id: str) -> bool:
        return execution_id in self._cancelled

    def store_result(self, result: ExecutionResult) -> None:
        self._results[result.execution_id] = result
        if result.execution_id not in self._cancelled:
            self._status[result.execution_id] = JobStatus(result.status)

    def get_status(self, execution_id: str) -> JobStatus | None:
        return self._status.get(execution_id)

    def get_result(self, execution_id: str) -> ExecutionResult | None:
        return self._results.get(execution_id)

    def get_request(self, execution_id: str) -> ExecutionRequest | None:
        return self._requests.get(execution_id)

    def drop(self, execution_id: str) -> None:
        self._requests.pop(execution_id, None)
        self._results.pop(execution_id, None)
        self._status.pop(execution_id, None)
        self._cancelled.discard(execution_id)


class RedisExecutionQueue:
    """Thin Redis list + hash adapter for multi-process trusted development."""

    def __init__(self, redis_url: str) -> None:
        import redis

        self._client = redis.Redis.from_url(redis_url, decode_responses=True)
        self._memory = MemoryExecutionQueue()

    def enqueue(self, request: ExecutionRequest) -> None:
        self._memory.enqueue(request)
        self._client.rpush("nh:exec:queue", request.execution_id)
        self._client.hset(
            f"nh:exec:req:{request.execution_id}",
            mapping={"payload": json.dumps(_request_to_json(request))},
        )

    def dequeue(self) -> ExecutionRequest | None:
        item = self._client.lpop("nh:exec:queue")
        if item is None:
            return self._memory.dequeue()
        return self._memory.get_request(str(item))

    def mark_cancelled(self, execution_id: str) -> None:
        self._memory.mark_cancelled(execution_id)
        self._client.set(f"nh:exec:cancel:{execution_id}", "1")

    def is_cancelled(self, execution_id: str) -> bool:
        return self._memory.is_cancelled(execution_id) or bool(
            self._client.get(f"nh:exec:cancel:{execution_id}")
        )

    def store_result(self, result: ExecutionResult) -> None:
        self._memory.store_result(result)
        key = f"nh:exec:res:{result.execution_id}"
        self._client.set(key, json.dumps(result.to_dict()))

    def get_status(self, execution_id: str) -> JobStatus | None:
        return self._memory.get_status(execution_id)

    def get_result(self, execution_id: str) -> ExecutionResult | None:
        return self._memory.get_result(execution_id)

    def get_request(self, execution_id: str) -> ExecutionRequest | None:
        return self._memory.get_request(execution_id)

    def drop(self, execution_id: str) -> None:
        self._memory.drop(execution_id)
        self._client.delete(
            f"nh:exec:req:{execution_id}",
            f"nh:exec:res:{execution_id}",
            f"nh:exec:cancel:{execution_id}",
        )


def _request_to_json(request: ExecutionRequest) -> dict[str, Any]:
    return {
        "execution_id": request.execution_id,
        "mission_id": request.mission_id,
        "mission_version": request.mission_version,
        "runtime": request.runtime,
        "environment_id": request.environment_id,
        "files": [asdict(f) for f in request.files],
        "limits": asdict(request.limits),
    }
