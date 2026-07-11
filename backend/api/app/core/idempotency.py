"""Idempotency helpers backed by the Redis adapter."""

from __future__ import annotations

import json
from typing import Any

from fastapi import HTTPException, status

from api.app.redis.client import KeyValueStore


class IdempotencyService:
    def __init__(self, store: KeyValueStore, ttl_seconds: int) -> None:
        self._store = store
        self._ttl = ttl_seconds

    def _key(self, scope: str, profile_id: str, idem_key: str) -> str:
        return f"idem:{scope}:{profile_id}:{idem_key}"

    def get_cached(
        self, scope: str, profile_id: str, idem_key: str
    ) -> dict[str, Any] | None:
        raw = self._store.get(self._key(scope, profile_id, idem_key))
        if raw is None:
            return None
        return json.loads(raw)

    def put(
        self, scope: str, profile_id: str, idem_key: str, payload: dict[str, Any]
    ) -> None:
        self._store.set(
            self._key(scope, profile_id, idem_key),
            json.dumps(payload),
            ex=self._ttl,
        )

    def require_key(self, idem_key: str | None) -> str:
        if not idem_key or not idem_key.strip():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail={
                    "error": {
                        "code": "IDEMPOTENCY_KEY_REQUIRED",
                        "message": "Idempotency-Key header or body field is required",
                    }
                },
            )
        return idem_key.strip()
