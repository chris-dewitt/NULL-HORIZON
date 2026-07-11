"""Normalization and truncation unit tests."""

from __future__ import annotations

from runner.normalize import (
    normalize_cpp_output,
    normalize_pytest_output,
    truncate_output,
)


def test_truncate_output_marks_overflow() -> None:
    text, truncated = truncate_output("abcdefghij", max_bytes=4)
    assert truncated is True
    assert "[truncated]" in text
    assert len(text.encode("utf-8")) <= 4 + len(b"\n[truncated]\n")


def test_normalize_pytest_timeout() -> None:
    result = normalize_pytest_output(
        execution_id="e1",
        returncode=124,
        stdout="",
        stderr="",
        max_output_bytes=1000,
        timed_out=True,
    )
    assert result.outcome == "timeout"
    assert result.status == "failed"


def test_normalize_pytest_parses_lines() -> None:
    result = normalize_pytest_output(
        execution_id="e1",
        returncode=0,
        stdout="test_ok PASSED\ntest_other PASSED\n",
        stderr="",
        max_output_bytes=10_000,
    )
    assert result.outcome == "passed"
    assert [t.id for t in result.tests] == ["test_ok", "test_other"]


def test_normalize_cpp_compile_error() -> None:
    result = normalize_cpp_output(
        execution_id="e1",
        compile_returncode=1,
        run_returncode=None,
        stdout="",
        stderr="error: boom",
        max_output_bytes=1000,
    )
    assert result.outcome == "compile_error"
