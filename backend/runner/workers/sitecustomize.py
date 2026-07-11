"""Auto-loaded when the workers directory is on PYTHONPATH.

Python's site module imports top-level ``sitecustomize`` after startup.
Placing this file beside ``network_guard.py`` and putting that directory on
``PYTHONPATH`` installs the socket block for learner jobs.
"""

from __future__ import annotations

from network_guard import install

install()
