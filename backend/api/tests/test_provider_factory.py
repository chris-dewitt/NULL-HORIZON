"""API provider factory and hardened block tests."""

from __future__ import annotations

import pytest
from api.app.core.config import Settings
from api.app.main import create_app
from api.app.providers.factory import build_api_execution_provider
from api.app.providers.fake import FakeExecutionProvider
from api.app.providers.hardened import HardenedBlockedApiAdapter
from api.app.schemas import ExecutionSubmitRequest
from api.tests.conftest import create_profile
from fastapi.testclient import TestClient
from runner.contracts.provider import DeploymentBlockedError


def test_factory_defaults_to_fake() -> None:
    assert isinstance(build_api_execution_provider("fake"), FakeExecutionProvider)


def test_factory_hardened_adapter_raises() -> None:
    adapter = build_api_execution_provider("hardened")
    assert isinstance(adapter, HardenedBlockedApiAdapter)
    with pytest.raises(DeploymentBlockedError):
        adapter.build_result(
            "e1",
            ExecutionSubmitRequest(
                mission_id="m",
                mission_version="1.0.0",
                runtime="python",
                environment_id="e",
                files=[],
            ),
        )


def test_hardened_provider_returns_503() -> None:
    app = create_app(
        Settings(
            execution_provider="hardened",
            database_url="sqlite+pysqlite:///:memory:",
        )
    )
    client = TestClient(app)
    profile = create_profile(client)
    response = client.post(
        "/v1/executions",
        headers={
            "Authorization": f"Bearer {profile['access_token']}",
            "Idempotency-Key": "blocked-1",
        },
        json={
            "mission_id": "m",
            "mission_version": "1.0.0",
            "runtime": "python",
            "environment_id": "e",
            "files": [{"path": "a.py", "content": "x = 1\n"}],
        },
    )
    assert response.status_code == 503
    assert response.json()["detail"]["error"]["code"] == "EXECUTION_BLOCKED"
