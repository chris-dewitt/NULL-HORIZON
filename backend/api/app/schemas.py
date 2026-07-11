"""Pydantic request/response schemas for Epic 7 endpoints."""

from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: Literal["ok"] = "ok"
    service: str
    version: str
    execution_provider: str
    database: Literal["ok", "error"] = "ok"
    redis: Literal["ok", "error", "memory"] = "memory"


class AnonymousProfileRequest(BaseModel):
    display_name: str = Field(min_length=1, max_length=64)


class AnonymousProfileResponse(BaseModel):
    profile_id: str
    display_name: str
    access_token: str


class MissionProgressItem(BaseModel):
    mission_id: str
    status: str
    best_hint_level: int = 0
    clearance_awarded: int = 0
    attempt_count: int = 1
    mission_version: str = "1.0.0"


class SkillEvidenceItem(BaseModel):
    event_id: str
    skill_id: str
    mission_id: str
    assisted: bool = False
    delta: int = Field(default=1, ge=1)


class SkillMasteryItem(BaseModel):
    skill_id: str
    mastery_level: str
    evidence_count: int
    unassisted_evidence_count: int
    last_practiced_at_epoch_ms: int = 0


class RewardItem(BaseModel):
    reward_id: str
    equipped: bool = False


class ReviewRecommendationItem(BaseModel):
    skill_id: str
    reason: str


class ProgressResponse(BaseModel):
    profile_id: str
    rank: str = "Emergency Operator"
    clearance_points: int = 0
    missions: list[MissionProgressItem] = []
    skills: list[SkillMasteryItem] = []
    rewards: list[RewardItem] = []
    review_recommendations: list[ReviewRecommendationItem] = []


class UpsertMissionProgressRequest(BaseModel):
    status: Literal["completed"] = "completed"
    best_hint_level: int = Field(default=0, ge=0)
    clearance_awarded: int = Field(default=0, ge=0)
    attempt_count: int = Field(default=1, ge=1)
    mission_version: str = "1.0.0"


class ProgressSyncRequest(BaseModel):
    idempotency_key: str | None = None
    missions: list[MissionProgressItem] = []
    skill_evidence: list[SkillEvidenceItem] = []
    rewards: list[RewardItem] = []


class ProgressSyncResponse(BaseModel):
    profile_id: str
    accepted: int
    rank: str
    clearance_points: int
    missions: list[MissionProgressItem]
    skills: list[SkillMasteryItem]
    rewards: list[RewardItem]
    review_recommendations: list[ReviewRecommendationItem] = []


class ContentManifestResponse(BaseModel):
    schema_version: int
    bundle_id: str
    version: str
    min_app_version: str
    content_schema_version: int
    locale: str
    channel: str
    chapters: list[str] = []
    missions: list[str] = []
    skills: list[str] = []
    dialogues: list[str] = []
    rewards: list[str] = []


class ExecutionFile(BaseModel):
    path: str
    content: str


class ExecutionSubmitRequest(BaseModel):
    idempotency_key: str | None = None
    mission_id: str
    mission_version: str
    runtime: Literal["python", "cpp"] = "python"
    environment_id: str
    files: list[ExecutionFile]


class ExecutionReceipt(BaseModel):
    execution_id: str
    status: str
    poll_after_ms: int = 0


class ExecutionTestResult(BaseModel):
    id: str
    visibility: Literal["visible", "hidden"] = "visible"
    status: str
    message: str | None = None


class ExecutionResultResponse(BaseModel):
    execution_id: str
    status: str
    outcome: str
    stdout: str = ""
    stderr: str = ""
    diagnostics: list[dict[str, Any]] = []
    tests: list[ExecutionTestResult] = []
    metrics: dict[str, Any] = {}
    truncated: bool = False
