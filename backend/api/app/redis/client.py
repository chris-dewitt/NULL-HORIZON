"""Redis adapter with an in-memory fallback for hermetic tests."""

from __future__ import annotations

import time
from typing import Protocol


class KeyValueStore(Protocol):
    def get(self, key: str) -> str | None: ...

    def set(self, key: str, value: str, ex: int | None = None) -> None: ...

    def delete(self, key: str) -> None: ...

    def ping(self) -> bool: ...


class MemoryKeyValueStore:
    def __init__(self) -> None:
        self._data: dict[str, tuple[str, float | None]] = {}

    def get(self, key: str) -> str | None:
        item = self._data.get(key)
        if item is None:
            return None
        value, expires_at = item
        if expires_at is not None and time.time() >= expires_at:
            del self._data[key]
            return None
        return value

    def set(self, key: str, value: str, ex: int | None = None) -> None:
        expires_at = time.time() + ex if ex is not None else None
        self._data[key] = (value, expires_at)

    def delete(self, key: str) -> None:
        self._data.pop(key, None)

    def ping(self) -> bool:
        return True


class RedisKeyValueStore:
    def __init__(self, url: str) -> None:
        import redis

        self._client = redis.Redis.from_url(url, decode_responses=True)

    def get(self, key: str) -> str | None:
        value = self._client.get(key)
        return value if isinstance(value, str) else None

    def set(self, key: str, value: str, ex: int | None = None) -> None:
        self._client.set(key, value, ex=ex)

    def delete(self, key: str) -> None:
        self._client.delete(key)

    def ping(self) -> bool:
        return bool(self._client.ping())


def create_kv_store(redis_url: str) -> KeyValueStore:
    if redis_url.strip():
        return RedisKeyValueStore(redis_url)
    return MemoryKeyValueStore()
