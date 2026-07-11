"""OS limit helpers applied in worker child processes."""

from __future__ import annotations

import resource
from collections.abc import Callable

from runner.contracts.models import ExecutionLimits


def apply_resource_limits(limits: ExecutionLimits) -> None:
    """Best-effort rlimits for trusted-development workers.

    Applied only in the worker child via ``preexec_fn`` — never in the API
    process or the pytest parent.
    """
    # Address space (soft memory ceiling). Keep a floor so interpreters/toolchains
    # can map shared libraries (clang needs well above 128MB).
    memory_bytes = max(limits.memory_mb, 256) * 1024 * 1024
    _set_limit(resource.RLIMIT_AS, memory_bytes)
    # Avoid tightening RLIMIT_NPROC here: clang/pytest spawn helper processes and
    # a low per-UID cap breaks posix_spawn on busy hosts. Hardened sandboxes will
    # enforce process caps via cgroups. Still refuse core dumps.
    _set_limit(resource.RLIMIT_CORE, 0)


def make_preexec(limits: ExecutionLimits) -> Callable[[], None]:
    def _preexec() -> None:
        apply_resource_limits(limits)

    return _preexec


def _set_limit(limit_id: int, value: int) -> None:
    try:
        soft, hard = resource.getrlimit(limit_id)
        target = value
        if hard != resource.RLIM_INFINITY:
            target = min(value, hard)
        hard_bound = hard if hard != resource.RLIM_INFINITY else target
        resource.setrlimit(limit_id, (target, hard_bound))
    except (ValueError, OSError):
        # Some hosts disallow rlimits; timeout/output limits still apply.
        return
