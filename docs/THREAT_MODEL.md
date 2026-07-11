# Threat Model (Skeleton)

Authoritative detail lives in [PRODUCT_SPEC.md](PRODUCT_SPEC.md) §25. This file tracks the living threat-model summary.

## Assets

- Player account and progress
- Content signing keys
- Execution infrastructure
- API credentials
- Administrative publishing access
- Other players' code and data
- Mission answer integrity
- Application signing keys
- Telemetry data

## Primary threats

- Account takeover and token theft
- API abuse and denial of service
- Runner escape from learner code
- Content-bundle tampering
- Mission-answer extraction
- Secret leakage and insecure local storage
- Dependency compromise

## Required controls (summary)

- HTTPS only; no embedded backend secrets in the client
- Learner code treated as hostile and never run in the API process
- Isolated runners with no Docker socket, no privileged mode, and no network by default
- Signed content manifests and checksum verification
- Rate limits, schema validation, and audit logging on the API
- Defensive-only cybersecurity curriculum content

## Open decisions

Production sandbox technology, authentication provider, and content-signing key custody remain open questions recorded in the product specification §41 and upcoming ADRs.
