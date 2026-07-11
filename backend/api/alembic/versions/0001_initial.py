"""Initial Epic 7 schema."""

from __future__ import annotations

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0001_initial"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "profiles",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column("display_name", sa.String(length=128), nullable=False),
        sa.Column(
            "access_token_hash", sa.String(length=128), nullable=False, unique=True
        ),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_table(
        "mission_completions",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "profile_id",
            sa.String(length=64),
            sa.ForeignKey("profiles.id"),
            nullable=False,
        ),
        sa.Column("mission_id", sa.String(length=128), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("best_hint_level", sa.Integer(), nullable=False),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.UniqueConstraint("profile_id", "mission_id", name="uq_profile_mission"),
    )
    op.create_table(
        "progress_snapshots",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "profile_id",
            sa.String(length=64),
            sa.ForeignKey("profiles.id"),
            nullable=False,
        ),
        sa.Column("payload", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_table(
        "execution_jobs",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "profile_id",
            sa.String(length=64),
            sa.ForeignKey("profiles.id"),
            nullable=False,
        ),
        sa.Column("mission_id", sa.String(length=128), nullable=False),
        sa.Column("mission_version", sa.String(length=32), nullable=False),
        sa.Column("runtime", sa.String(length=32), nullable=False),
        sa.Column("environment_id", sa.String(length=128), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("request_files", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_table(
        "execution_results",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "execution_id",
            sa.String(length=64),
            sa.ForeignKey("execution_jobs.id"),
            nullable=False,
            unique=True,
        ),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("outcome", sa.String(length=64), nullable=False),
        sa.Column("stdout", sa.Text(), nullable=False),
        sa.Column("stderr", sa.Text(), nullable=False),
        sa.Column("tests", sa.JSON(), nullable=False),
        sa.Column("metrics", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_table(
        "audit_events",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column("event_type", sa.String(length=64), nullable=False),
        sa.Column("profile_id", sa.String(length=64), nullable=True),
        sa.Column("detail", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )


def downgrade() -> None:
    op.drop_table("audit_events")
    op.drop_table("execution_results")
    op.drop_table("execution_jobs")
    op.drop_table("progress_snapshots")
    op.drop_table("mission_completions")
    op.drop_table("profiles")
