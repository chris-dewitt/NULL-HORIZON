"""Epic 9 progression tables and profile rank/clearance columns."""

from __future__ import annotations

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0002_progression"
down_revision: str | None = "0001_initial"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "profiles",
        sa.Column(
            "rank",
            sa.String(length=64),
            nullable=False,
            server_default="Emergency Operator",
        ),
    )
    op.add_column(
        "profiles",
        sa.Column(
            "clearance_points",
            sa.Integer(),
            nullable=False,
            server_default="0",
        ),
    )
    op.add_column(
        "mission_completions",
        sa.Column(
            "clearance_awarded",
            sa.Integer(),
            nullable=False,
            server_default="0",
        ),
    )
    op.add_column(
        "mission_completions",
        sa.Column(
            "attempt_count",
            sa.Integer(),
            nullable=False,
            server_default="1",
        ),
    )
    op.add_column(
        "mission_completions",
        sa.Column(
            "mission_version",
            sa.String(length=32),
            nullable=False,
            server_default="1.0.0",
        ),
    )
    op.create_table(
        "skill_evidence",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "profile_id",
            sa.String(length=64),
            sa.ForeignKey("profiles.id"),
            nullable=False,
        ),
        sa.Column("event_id", sa.String(length=128), nullable=False),
        sa.Column("skill_id", sa.String(length=128), nullable=False),
        sa.Column("mission_id", sa.String(length=128), nullable=False),
        sa.Column("assisted", sa.Boolean(), nullable=False),
        sa.Column("delta", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.UniqueConstraint("profile_id", "event_id", name="uq_profile_evidence_event"),
    )
    op.create_table(
        "reward_unlocks",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column(
            "profile_id",
            sa.String(length=64),
            sa.ForeignKey("profiles.id"),
            nullable=False,
        ),
        sa.Column("reward_id", sa.String(length=128), nullable=False),
        sa.Column("unlocked_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("equipped", sa.Boolean(), nullable=False),
        sa.UniqueConstraint("profile_id", "reward_id", name="uq_profile_reward"),
    )


def downgrade() -> None:
    op.drop_table("reward_unlocks")
    op.drop_table("skill_evidence")
    op.drop_column("mission_completions", "mission_version")
    op.drop_column("mission_completions", "attempt_count")
    op.drop_column("mission_completions", "clearance_awarded")
    op.drop_column("profiles", "clearance_points")
    op.drop_column("profiles", "rank")
