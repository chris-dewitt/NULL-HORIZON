# Security Policy

## Reporting a vulnerability

Do not open a public issue for security vulnerabilities.

Email the maintainers with:

- A description of the issue
- Steps to reproduce
- Affected component (`android-app`, `backend/api`, `backend/runner`, content pipeline, or infrastructure)
- Impact assessment if known

If a private security contact is not yet published, use the repository owner's contact channel and mark the report as security-sensitive.

## Scope

In scope:

- Authentication and authorization flaws
- Secret leakage
- Content-bundle tampering
- Execution isolation failures
- Path traversal or workspace escape in runners
- Dependency compromise indicators in this repository

Out of scope for this policy:

- Attacks against third-party services unrelated to this project
- Social engineering of individual contributors
- Issues that require physical access to an unlocked device

## Project security principles

- Minimize collected data and default to local play.
- Treat all client input as untrusted.
- Treat learner-authored code as hostile.
- Keep secrets out of the repository.
- Separate application and execution trust boundaries.
- Never execute learner code in the API process.
- Never mount the Docker socket into learner runners.
- Never grant privileged container access for public learner execution.

## Supported versions

Only the `main` branch and tagged releases receive security fixes until a public release channel exists.

## Disclosure

Maintainers will acknowledge reports when possible, assess impact, and coordinate a fix before public disclosure when the issue is confirmed.
