"""Content manifest and bundle routes."""

from __future__ import annotations

import json
from pathlib import Path

from fastapi import APIRouter, HTTPException, Request, status

from api.app.schemas import ContentManifestResponse

router = APIRouter(tags=["content"])


def _manifest_path(request: Request) -> Path:
    return request.app.state.settings.resolved_content_dir() / "manifest.json"


@router.get("/v1/content/manifest", response_model=ContentManifestResponse)
def get_manifest(request: Request) -> ContentManifestResponse:
    path = _manifest_path(request)
    if not path.is_file():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "error": {
                    "code": "CONTENT_MANIFEST_MISSING",
                    "message": f"Manifest not found at {path}",
                }
            },
        )
    payload = json.loads(path.read_text(encoding="utf-8"))
    return ContentManifestResponse.model_validate(payload)


@router.get("/v1/content/bundles/{bundle_id}")
def get_bundle(bundle_id: str, request: Request) -> dict[str, object]:
    root = request.app.state.settings.resolved_content_dir()
    manifest_path = root / "manifest.json"
    if not manifest_path.is_file():
        raise HTTPException(status_code=404, detail={"error": {"code": "NOT_FOUND"}})
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("bundle_id") != bundle_id:
        raise HTTPException(
            status_code=404,
            detail={"error": {"code": "BUNDLE_NOT_FOUND", "message": bundle_id}},
        )
    checksums_path = root / "checksums.json"
    checksums = (
        json.loads(checksums_path.read_text(encoding="utf-8"))
        if checksums_path.is_file()
        else {"files": {}}
    )
    return {
        "bundle_id": bundle_id,
        "manifest": manifest,
        "checksums": checksums,
        "root": str(root),
    }
