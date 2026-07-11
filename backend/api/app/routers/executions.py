"""Execution job API. Learner code is never executed in this process."""

from __future__ import annotations

import secrets
from datetime import UTC, datetime
from typing import Annotated

from fastapi import APIRouter, Depends, Header, HTTPException, status
from runner.contracts.provider import DeploymentBlockedError
from sqlalchemy import select
from sqlalchemy.orm import Session

from api.app.core.idempotency import IdempotencyService
from api.app.db.models import ExecutionJob, ExecutionResultRow, Profile
from api.app.deps import (
    get_current_profile,
    get_db,
    get_execution_provider,
    get_idempotency,
)
from api.app.providers.base import ExecutionProvider
from api.app.schemas import (
    ExecutionReceipt,
    ExecutionResultResponse,
    ExecutionSubmitRequest,
)

router = APIRouter(tags=["executions"])


@router.post("/v1/executions", response_model=ExecutionReceipt)
def submit_execution(
    body: ExecutionSubmitRequest,
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
    idem: Annotated[IdempotencyService, Depends(get_idempotency)],
    provider: Annotated[ExecutionProvider, Depends(get_execution_provider)],
    idempotency_key: Annotated[str | None, Header(alias="Idempotency-Key")] = None,
) -> ExecutionReceipt:
    key = idem.require_key(idempotency_key or body.idempotency_key)
    cached = idem.get_cached("execution_submit", profile.id, key)
    if cached is not None:
        return ExecutionReceipt.model_validate(cached)

    execution_id = f"exe_{secrets.token_urlsafe(12)}"
    now = datetime.now(UTC)
    job = ExecutionJob(
        id=execution_id,
        profile_id=profile.id,
        mission_id=body.mission_id,
        mission_version=body.mission_version,
        runtime=body.runtime,
        environment_id=body.environment_id,
        status="completed",
        request_files=[item.model_dump() for item in body.files],
        created_at=now,
        updated_at=now,
    )
    try:
        result = provider.build_result(execution_id, body)
    except DeploymentBlockedError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={
                "error": {
                    "code": "EXECUTION_BLOCKED",
                    "message": str(exc),
                }
            },
        ) from exc
    row = ExecutionResultRow(
        id=f"res_{secrets.token_urlsafe(10)}",
        execution_id=execution_id,
        status=result.status,
        outcome=result.outcome,
        stdout=result.stdout,
        stderr=result.stderr,
        tests=[t.model_dump() for t in result.tests],
        metrics=result.metrics,
        created_at=now,
    )
    session.add(job)
    session.add(row)
    session.flush()

    receipt = ExecutionReceipt(
        execution_id=execution_id, status=result.status, poll_after_ms=0
    )
    idem.put("execution_submit", profile.id, key, receipt.model_dump())
    return receipt


@router.get("/v1/executions/{execution_id}", response_model=ExecutionResultResponse)
def get_execution(
    execution_id: str,
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> ExecutionResultResponse:
    job = session.scalar(select(ExecutionJob).where(ExecutionJob.id == execution_id))
    if job is None or job.profile_id != profile.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"error": {"code": "EXECUTION_NOT_FOUND"}},
        )
    result = session.scalar(
        select(ExecutionResultRow).where(
            ExecutionResultRow.execution_id == execution_id
        )
    )
    if result is None:
        return ExecutionResultResponse(
            execution_id=execution_id,
            status=job.status,
            outcome="pending",
        )
    return ExecutionResultResponse(
        execution_id=execution_id,
        status=result.status,
        outcome=result.outcome,
        stdout=result.stdout,
        stderr=result.stderr,
        tests=result.tests,  # type: ignore[arg-type]
        metrics=result.metrics,
    )


@router.delete("/v1/executions/{execution_id}", status_code=status.HTTP_204_NO_CONTENT)
def cancel_execution(
    execution_id: str,
    session: Annotated[Session, Depends(get_db)],
    profile: Annotated[Profile, Depends(get_current_profile)],
) -> None:
    job = session.scalar(select(ExecutionJob).where(ExecutionJob.id == execution_id))
    if job is None or job.profile_id != profile.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"error": {"code": "EXECUTION_NOT_FOUND"}},
        )
    # Fake provider completes immediately; cancel is a no-op success for completed jobs.
    if job.status not in {"completed", "cancelled"}:
        job.status = "cancelled"
        job.updated_at = datetime.now(UTC)
