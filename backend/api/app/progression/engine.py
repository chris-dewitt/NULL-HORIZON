"""Deterministic progression helpers shared by API sync routes."""

from __future__ import annotations

from dataclasses import dataclass

MASTER_LEVELS = ("none", "introduced", "practiced", "reliable", "mastered")

RANK_THRESHOLDS: tuple[tuple[int, str], ...] = (
    (0, "Emergency Operator"),
    (50, "Maintenance Technician"),
    (150, "Systems Investigator"),
    (300, "Automation Engineer"),
    (500, "Backend Engineer"),
    (800, "Reliability Engineer"),
    (1200, "Infrastructure Architect"),
    (1800, "Horizon Core Administrator"),
)


def mastery_level(*, evidence_count: int, unassisted_evidence_count: int) -> str:
    if unassisted_evidence_count >= 3:
        return "mastered"
    if unassisted_evidence_count >= 2:
        return "reliable"
    if evidence_count >= 2:
        return "practiced"
    if evidence_count >= 1:
        return "introduced"
    return "none"


def rank_for_clearance(clearance_points: int) -> str:
    title = RANK_THRESHOLDS[0][1]
    for threshold, name in RANK_THRESHOLDS:
        if clearance_points >= threshold:
            title = name
    return title


@dataclass(frozen=True, slots=True)
class ReviewRecommendation:
    skill_id: str
    reason: str


def review_recommendations(
    skills: list[dict[str, object]],
    *,
    now_epoch_ms: int,
    stale_after_ms: int = 7 * 24 * 60 * 60 * 1000,
) -> list[ReviewRecommendation]:
    """Suggest review for under-practiced or stale skills."""
    out: list[ReviewRecommendation] = []
    for skill in skills:
        skill_id = str(skill["skill_id"])
        level = str(skill.get("mastery_level", "none"))
        raw_last = skill.get("last_practiced_at_epoch_ms") or 0
        last = int(raw_last) if isinstance(raw_last, (int, float, str)) else 0
        if level in {"introduced", "practiced"}:
            out.append(
                ReviewRecommendation(
                    skill_id=skill_id,
                    reason="Build reliability with another unassisted repair",
                )
            )
        elif last > 0 and now_epoch_ms - last >= stale_after_ms:
            out.append(
                ReviewRecommendation(
                    skill_id=skill_id,
                    reason="Skill has not been practiced recently",
                )
            )
    return out
