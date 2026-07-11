"""API-facing adapters that never execute learner code in-process."""

from __future__ import annotations

from api.app.providers.fake import FakeExecutionProvider
from api.app.providers.hardened import HardenedBlockedApiAdapter
from api.app.providers.local_trusted import LocalTrustedApiAdapter

ApiExecutionProvider = (
    FakeExecutionProvider | LocalTrustedApiAdapter | HardenedBlockedApiAdapter
)


def build_api_execution_provider(name: str) -> ApiExecutionProvider:
    if name == "fake":
        return FakeExecutionProvider()
    if name == "local_trusted":
        return LocalTrustedApiAdapter()
    if name in {"hardened", "production"}:
        return HardenedBlockedApiAdapter()
    raise ValueError(f"Unknown execution provider: {name}")
