"""Provider boundary tests."""

from __future__ import annotations

import pytest
from runner.contracts.models import ExecutionFile, ExecutionLimits, ExecutionRequest
from runner.contracts.provider import DeploymentBlockedError
from runner.providers import HardenedSandboxProvider, build_provider


def _sample_request() -> ExecutionRequest:
    return ExecutionRequest(
        execution_id="x",
        mission_id="m",
        mission_version="1.0.0",
        runtime="python",
        environment_id="e",
        files=[ExecutionFile(path="t.py", content="")],
        limits=ExecutionLimits(),
    )


def test_hardened_provider_blocked() -> None:
    provider = HardenedSandboxProvider()
    with pytest.raises(DeploymentBlockedError, match="security review"):
        provider.submit(_sample_request())


def test_build_provider_hardened() -> None:
    provider = build_provider("hardened")
    with pytest.raises(DeploymentBlockedError):
        provider.submit(_sample_request())


def test_build_provider_unknown() -> None:
    with pytest.raises(ValueError, match="Unknown"):
        build_provider("nope")
