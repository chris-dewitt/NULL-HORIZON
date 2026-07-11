"""Ephemeral per-job workspaces and retention cleanup."""

from __future__ import annotations

import shutil
import time
from pathlib import Path

from runner.contracts.models import ExecutionFile


class WorkspaceManager:
    def __init__(self, root: Path | None = None) -> None:
        self.root = root or Path("/tmp/nullhorizon-exec")  # noqa: S108
        self.root.mkdir(parents=True, exist_ok=True)

    def create(self, execution_id: str, files: list[ExecutionFile]) -> Path:
        path = self.root / execution_id
        if path.exists():
            shutil.rmtree(path)
        path.mkdir(parents=True, exist_ok=False)
        path.chmod(0o700)
        for item in files:
            # Prevent path escape from the workspace.
            target = (path / item.path.lstrip("/")).resolve()
            if not str(target).startswith(str(path.resolve())):
                raise ValueError(f"Illegal workspace path: {item.path}")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(item.content, encoding="utf-8")
        return path

    def destroy(self, execution_id: str) -> None:
        path = self.root / execution_id
        if path.exists():
            shutil.rmtree(path, ignore_errors=True)

    def cleanup_expired(self, retention_sec: int) -> list[str]:
        removed: list[str] = []
        now = time.time()
        for child in self.root.iterdir():
            if not child.is_dir():
                continue
            age = now - child.stat().st_mtime
            if age >= retention_sec:
                shutil.rmtree(child, ignore_errors=True)
                removed.append(child.name)
        return removed

    def path_for(self, execution_id: str) -> Path:
        return self.root / execution_id
