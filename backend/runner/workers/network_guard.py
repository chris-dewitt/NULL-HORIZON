"""Network guard injected into Python worker environments via sitecustomize."""

from __future__ import annotations

import socket

_REAL_CONNECT = socket.socket.connect


def _blocked_connect(self, *args, **kwargs):  # type: ignore[no-untyped-def]
    raise OSError("Network disabled in NULL HORIZON mission runner")


def install() -> None:
    socket.socket.connect = _blocked_connect  # type: ignore[method-assign]


def uninstall() -> None:
    socket.socket.connect = _REAL_CONNECT  # type: ignore[method-assign]
