"""PostgreSQL / SQLite ORM models for Epic 7–9."""

from __future__ import annotations

from datetime import UTC, datetime
from typing import Any

from sqlalchemy import (
    JSON,
    Boolean,
    DateTime,
    ForeignKey,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from api.app.db.base import Base


def utcnow() -> datetime:
    return datetime.now(UTC)


class Profile(Base):
    __tablename__ = "profiles"

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    display_name: Mapped[str] = mapped_column(String(128), nullable=False)
    access_token_hash: Mapped[str] = mapped_column(
        String(128), unique=True, nullable=False
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )
    rank: Mapped[str] = mapped_column(
        String(64), default="Emergency Operator", nullable=False
    )
    clearance_points: Mapped[int] = mapped_column(Integer, default=0, nullable=False)

    mission_progress: Mapped[list[MissionProgress]] = relationship(
        back_populates="profile"
    )
    skill_evidence: Mapped[list[SkillEvidence]] = relationship(back_populates="profile")
    reward_unlocks: Mapped[list[RewardUnlock]] = relationship(back_populates="profile")
    execution_jobs: Mapped[list[ExecutionJob]] = relationship(back_populates="profile")


class MissionProgress(Base):
    __tablename__ = "mission_completions"
    __table_args__ = (
        UniqueConstraint("profile_id", "mission_id", name="uq_profile_mission"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    profile_id: Mapped[str] = mapped_column(ForeignKey("profiles.id"), nullable=False)
    mission_id: Mapped[str] = mapped_column(String(128), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="completed")
    best_hint_level: Mapped[int] = mapped_column(Integer, default=0)
    clearance_awarded: Mapped[int] = mapped_column(Integer, default=0)
    attempt_count: Mapped[int] = mapped_column(Integer, default=1)
    mission_version: Mapped[str] = mapped_column(String(32), default="1.0.0")
    completed_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )

    profile: Mapped[Profile] = relationship(back_populates="mission_progress")


class SkillEvidence(Base):
    __tablename__ = "skill_evidence"
    __table_args__ = (
        UniqueConstraint("profile_id", "event_id", name="uq_profile_evidence_event"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    profile_id: Mapped[str] = mapped_column(ForeignKey("profiles.id"), nullable=False)
    event_id: Mapped[str] = mapped_column(String(128), nullable=False)
    skill_id: Mapped[str] = mapped_column(String(128), nullable=False)
    mission_id: Mapped[str] = mapped_column(String(128), nullable=False)
    assisted: Mapped[bool] = mapped_column(Boolean, default=False)
    delta: Mapped[int] = mapped_column(Integer, default=1)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )

    profile: Mapped[Profile] = relationship(back_populates="skill_evidence")


class RewardUnlock(Base):
    __tablename__ = "reward_unlocks"
    __table_args__ = (
        UniqueConstraint("profile_id", "reward_id", name="uq_profile_reward"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    profile_id: Mapped[str] = mapped_column(ForeignKey("profiles.id"), nullable=False)
    reward_id: Mapped[str] = mapped_column(String(128), nullable=False)
    unlocked_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )
    equipped: Mapped[bool] = mapped_column(Boolean, default=False)

    profile: Mapped[Profile] = relationship(back_populates="reward_unlocks")


class ProgressSnapshot(Base):
    __tablename__ = "progress_snapshots"

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    profile_id: Mapped[str] = mapped_column(ForeignKey("profiles.id"), nullable=False)
    payload: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )


class ExecutionJob(Base):
    __tablename__ = "execution_jobs"

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    profile_id: Mapped[str] = mapped_column(ForeignKey("profiles.id"), nullable=False)
    mission_id: Mapped[str] = mapped_column(String(128), nullable=False)
    mission_version: Mapped[str] = mapped_column(String(32), nullable=False)
    runtime: Mapped[str] = mapped_column(String(32), nullable=False)
    environment_id: Mapped[str] = mapped_column(String(128), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="queued")
    request_files: Mapped[list[dict[str, str]]] = mapped_column(JSON, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )

    profile: Mapped[Profile] = relationship(back_populates="execution_jobs")
    result: Mapped[ExecutionResultRow | None] = relationship(
        back_populates="job", uselist=False
    )


class ExecutionResultRow(Base):
    __tablename__ = "execution_results"

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    execution_id: Mapped[str] = mapped_column(
        ForeignKey("execution_jobs.id"), unique=True
    )
    status: Mapped[str] = mapped_column(String(32), nullable=False)
    outcome: Mapped[str] = mapped_column(String(64), nullable=False)
    stdout: Mapped[str] = mapped_column(Text, default="")
    stderr: Mapped[str] = mapped_column(Text, default="")
    tests: Mapped[list[dict[str, Any]]] = mapped_column(JSON, default=list)
    metrics: Mapped[dict[str, Any]] = mapped_column(JSON, default=dict)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )

    job: Mapped[ExecutionJob] = relationship(back_populates="result")


class AuditEvent(Base):
    __tablename__ = "audit_events"

    id: Mapped[str] = mapped_column(String(64), primary_key=True)
    event_type: Mapped[str] = mapped_column(String(64), nullable=False)
    profile_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    detail: Mapped[dict[str, Any]] = mapped_column(JSON, default=dict)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utcnow
    )
