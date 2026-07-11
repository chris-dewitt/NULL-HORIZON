"""Shared execution contracts for API and runner workers."""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import StrEnum
from typing import Any


class JobStatus(StrEnum):
    QUEUED = "queued"
    RUNNING = "running"
    COMPLETED = "completed"
    CANCELLED = "cancelled"
    FAILED = "failed"


@dataclass(slots=True)
class ExecutionLimits:
    wall_timeout_sec: float = 5.0
    memory_mb: int = 512
    max_output_bytes: int = 64_000
    max_processes: int = 128
    allow_network: bool = False
    retention_sec: int = 3600


@dataclass(slots=True)
class ExecutionFile:
    path: str
    content: str


@dataclass(slots=True)
class ExecutionRequest:
    execution_id: str
    mission_id: str
    mission_version: str
    runtime: str
    environment_id: str
    files: list[ExecutionFile]
    limits: ExecutionLimits = field(default_factory=ExecutionLimits)


@dataclass(slots=True)
class TestCaseResult:
    id: str
    status: str
    message: str | None = None
    visibility: str = "visible"


@dataclass(slots=True)
class ExecutionResult:
    execution_id: str
    status: str
    outcome: str
    stdout: str = ""
    stderr: str = ""
    tests: list[TestCaseResult] = field(default_factory=list)
    metrics: dict[str, Any] = field(default_factory=dict)
    truncated: bool = False
    diagnostics: list[dict[str, Any]] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        return {
            "execution_id": self.execution_id,
            "status": self.status,
            "outcome": self.outcome,
            "stdout": self.stdout,
            "stderr": self.stderr,
            "tests": [
                {
                    "id": t.id,
                    "status": t.status,
                    "message": t.message,
                    "visibility": t.visibility,
                }
                for t in self.tests
            ],
            "metrics": self.metrics,
            "truncated": self.truncated,
            "diagnostics": self.diagnostics,
        }


@dataclass(slots=True)
class ExecutionReceipt:
    execution_id: str
    status: str
    poll_after_ms: int = 250
