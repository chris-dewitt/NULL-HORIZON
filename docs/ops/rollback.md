# Rollback process

## App / store

1. Halt closed-track rollout (Play Console halt).
2. Re-release previous known-good AAB if needed.
3. Document incident in `docs/release/` notes.

## Content bundle

1. Pin clients to previous `content` bundle version via manifest channel.
2. Rebuild and sync assets from the last green tag.
3. Invalidate CDN/cache if a hosted bundle is used later.

## API / runner

1. Redeploy previous API image tag.
2. Keep `EXECUTION_PROVIDER=fake` or `local_trusted` as configured; never flip `hardened` without ADR.
3. If executions misbehave: disable online execution via provider config and rely on offline fixtures.

## Data deletion incidents

If deletion APIs fail, disable sync endpoints and instruct players to use local Settings deletion until fixed.
