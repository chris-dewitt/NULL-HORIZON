"""Network guard unit tests."""

from __future__ import annotations

import socket

from runner.workers import network_guard


def test_network_guard_blocks_connect() -> None:
    network_guard.install()
    try:
        sock = socket.socket()
        try:
            try:
                sock.connect(("127.0.0.1", 9))
                raise AssertionError("expected OSError")
            except OSError as exc:
                assert "Network disabled" in str(exc)
        finally:
            sock.close()
    finally:
        network_guard.uninstall()
