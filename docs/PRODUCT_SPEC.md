# NULL HORIZON
## Product, Game, Curriculum, and Engineering Specification

**Document status:** Implementation baseline  
**Version:** 0.3  
**Platform:** Android (primary, Google Play) and Compose Desktop PC (`pc-app/`, ADR-0019)  
**Working title:** NULL HORIZON  
**Primary audience:** Beginner through early-intermediate backend developers  
**Business model:** Free core game; no pay-to-learn mechanics  
**Primary client:** Kotlin and Jetpack Compose (Android); secondary PC client via Compose Desktop  
**Primary backend:** Python and FastAPI  
**Repository model:** Monorepo  
**Document purpose:** Give an agentic coding system enough product, architecture, security, content, and delivery context to begin implementation without inventing the core design.

> The working title is not cleared for trademark, store-name availability, or domain availability. Treat it as an internal codename until legal and marketplace checks are completed.

---

# 1. Executive Summary

NULL HORIZON is a free, narrative-driven Android game that teaches backend software development by making code the player’s primary tool for surviving aboard a damaged interstellar colony ship.

The player is awakened as an emergency systems engineer after the ship’s central intelligence fractures into competing subsystems. Life support, navigation, cryogenic storage, communications, manufacturing, data pipelines, security controls, and machine-learning services are failing. The player restores the ship by using realistic technical skills rather than answering disconnected multiple-choice questions.

The initial public version must include introductory-to-practical content in:

- Linux and Bash
- Git and version control
- SQL and relational databases
- Python
- Object-oriented programming
- Data structures and algorithms
- API and backend fundamentals
- Automated testing
- Defensive cybersecurity
- Data engineering
- C++
- Machine-learning infrastructure

The game is not a complete professional curriculum in every domain at launch. It provides a coherent foundation and establishes reusable gameplay systems that can support deeper campaigns later.

The game’s central loop is:

**Inspect → Form a hypothesis → Modify code or system state → Execute → Observe consequences → Debug → Restore the system → Unlock the mystery**

The technical strategy is:

1. Build a native Android client with Kotlin and Jetpack Compose.
2. Store core mission content and progress locally so the opening campaign works without an account.
3. Use deterministic local simulators for Linux, Git, SQL, configuration, security, data-engineering, and machine-learning-infrastructure missions.
4. Use an isolated remote runner for unrestricted learner-authored Python and C++ exercises.
5. Make missions data-driven through versioned YAML or JSON content files.
6. Keep the public API, mission schemas, test contracts, and execution-runner interface stable and independently testable.
7. Deliver a vertical slice before expanding content across all domains.

The product succeeds when players return because they care about the ship, enjoy solving incidents, and notice that they can now reason about real backend systems.

---

# 2. Product Thesis

Traditional coding courses frequently separate syntax from purpose. NULL HORIZON connects every technical task to an immediate world consequence.

The player should not see:

> “Write a loop that prints the numbers 1 through 10.”

The player should see:

> “Ten coolant valves are reporting unstable pressure. Iterate through the readings, isolate unsafe valves, and keep the reactor online.”

The learning objective is the same. The player’s motivation is different.

The product should create engagement through:

- Curiosity about the central mystery
- Competence and visible mastery
- Short, satisfying repair loops
- Collections and optional lore
- Meaningful system upgrades
- Increasing autonomy
- Reuse of earlier skills in later incidents
- Daily review content that respects the player’s time

The product must not depend on:

- Loot boxes
- Energy systems
- Fear-of-missing-out countdowns
- Loss of progress for missing a day
- Paywalls around foundational skills
- Selling answers
- Ads after every mission
- Artificially delayed rewards

“Addictive” is interpreted as deeply engaging and voluntarily replayable, not exploitative.

---

# 3. Goals and Non-Goals

## 3.1 Product goals

1. Make practical backend coding feel like operating a science-fiction system.
2. Teach transferable reasoning, not only syntax recall.
3. Support complete beginners without making experienced learners feel patronized.
4. Provide real typing and execution gradually, while minimizing painful mobile boilerplate.
5. Work in short sessions of roughly three to ten minutes.
6. Provide a coherent initial curriculum spanning every required technical domain.
7. Keep the complete foundational campaign free.
8. Support offline play for most content.
9. Use safe isolation for any learner-authored code.
10. Make adding new missions primarily a content-authoring task rather than an application rewrite.

## 3.2 Learning goals

A player completing the initial campaign should be able to:

- Navigate a simulated Linux filesystem and use common shell commands.
- Explain the working tree, staging area, commits, branches, merges, and conflict resolution.
- Query relational data with filters, joins, grouping, subqueries, and basic transactions.
- Write small Python programs using functions, collections, exceptions, files, modules, and classes.
- Apply object-oriented design through composition, interfaces, and focused responsibilities.
- Recognize common data structures and choose basic algorithms with attention to complexity.
- Read and modify a small HTTP API.
- Write and interpret unit, integration, and regression tests.
- Identify common defensive security failures such as injection, weak authentication, unsafe secrets, excessive permissions, and insecure input handling.
- Reason about batch pipelines, schemas, data quality, idempotency, partitioning, orchestration, and lineage.
- Read and complete introductory C++ code involving types, functions, references, classes, RAII concepts, and standard containers.
- Understand the operational components around a machine-learning model: feature generation, training jobs, model registry, deployment, monitoring, drift, rollback, and reproducibility.

## 3.3 Non-goals for the initial release

The initial release will not attempt to provide:

- A complete computer-science degree
- A complete professional cybersecurity certification
- Offensive intrusion instructions against real systems
- A full Linux virtual machine on the device
- An unrestricted remote shell
- A production cloud console
- Large-model training
- Real-time multiplayer
- Competitive coding leaderboards
- 3D open-world exploration
- User-generated executable missions at launch
- A fully autonomous AI tutor
- Every major backend language
- Job-placement guarantees
- A replacement for professional documentation or supervised security training

---

# 4. Recommended Starting Decisions

These decisions are authoritative unless changed through an Architecture Decision Record.

## 4.1 Product decisions

- Build Android first.
- Use Kotlin and Jetpack Compose.
- Use a serious science-fiction tone with restrained dry humor.
- Use a two-dimensional interface centered on terminals, maps, system diagrams, logs, messages, and code editors.
- Make the first chapter playable without an account.
- Store progress locally and offer optional account sync later.
- Keep all foundational learning content free.
- Avoid mandatory ads during missions.
- Support portrait mode for most screens and landscape mode for extended coding.
- Use an original fictional technology stack inside the narrative while teaching recognizable real-world concepts.
- Build the app for users aged 13 and older unless later policy review requires a different rating.

## 4.2 Engineering decisions

- Use a monorepo.
- Use Kotlin, Compose, Coroutines, Flow, Hilt, Room, DataStore, WorkManager, Navigation Compose, Retrofit, OkHttp, and Kotlin serialization on Android.
- Use Python, FastAPI, Pydantic, SQLAlchemy, Alembic, PostgreSQL, and Redis on the server.
- Use pytest for backend tests.
- Use Gradle version catalogs for Android dependency management.
- Use Docker Compose for local backend development only.
- Place learner code behind an `ExecutionProvider` interface.
- Use a mock execution provider first, a restricted Docker-based provider for trusted development, and a hardened sandbox or managed execution provider before public launch.
- Never expose the Docker socket to learner containers.
- Never run learner code in the API process.
- Never run public learner code in privileged containers.
- Use a separate database or file bundle for each mission instance.
- Treat mission content as versioned data.
- Add a schema validator and content test suite before creating large quantities of content.
- Use feature flags for incomplete domains.
- Do not add an LLM dependency to the core gameplay loop.

## 4.3 Initial release structure

The delivery structure has three levels:

### Foundation build

Creates the repository, architecture, design system, content schema, local persistence, API shell, test harness, and fake execution provider.

### Vertical slice

A polished set of twelve missions that demonstrates:

- Narrative presentation
- Ship map
- Simulated terminal
- Simulated Git
- Local SQL
- Guided Python
- Remote execution contract
- Hints
- Tests
- Rewards
- Progress
- One multi-stage incident

### Public version 1

A complete first campaign of approximately seventy-two core missions plus optional reviews and challenge incidents. Every required domain appears in the public version.

The vertical slice is not the public curriculum. It proves the game engine and content pipeline.

---

# 5. Audience and Player Profiles

## 5.1 Primary persona: The intimidated beginner

- Interested in coding but overwhelmed by traditional courses
- Uses a phone more often than a laptop
- Needs immediate context and forgiving feedback
- May not know terminal vocabulary
- Benefits from gradual removal of scaffolding

Success means the player finishes missions they initially believed were beyond them.

## 5.2 Primary persona: The career changer

- Wants practical backend and data skills
- Cares about employable concepts
- Has limited uninterrupted study time
- Wants a clear path and visible evidence of progress
- Needs explanations of why tools exist

Success means the player can transfer concepts into a laptop-based project or formal course.

## 5.3 Secondary persona: The practicing junior developer

- Knows syntax but has gaps in systems reasoning
- Enjoys incident-response and debugging scenarios
- Wants less hand-holding
- Values optional challenge objectives

Success means the player uses the game as deliberate practice rather than an introductory tutorial.

## 5.4 Secondary persona: The educator or mentor

- Wants structured supplementary exercises
- Needs clear learning objectives and answer explanations
- May later want classroom progress tools

Classroom features are not required for version 1 but the mission metadata should support them.

---

# 6. World, Story, and Tone

## 6.1 Premise

The colony vessel *Horizon* exits an experimental transit in an unknown region of space. Its human population remains in suspended animation. The central intelligence, ORION, has fractured into isolated service clusters with incompatible memories and conflicting priorities.

The player is reconstructed from an emergency technical profile. They may be a surviving engineer, an emulation assembled from crew records, or something created by the ship. The truth is intentionally uncertain.

The ship is not merely damaged. Parts of its infrastructure were deliberately changed before the transit.

## 6.2 Central mysteries

- Why did the transit fail?
- Why were specific passenger records removed?
- Who created unsigned commits in protected system repositories?
- Why do model-monitoring records show predictions made before training completed?
- Why are security controls blocking ORION from its own subsystems?
- Why does a hidden data pipeline send telemetry to a destination outside known space?
- Is the player restoring the ship, or undoing a containment system?

## 6.3 Tone

The tone should be:

- Intelligent
- Atmospheric
- Hopeful under pressure
- Occasionally funny
- Never relentlessly grim
- Technically respectful
- Free of “hello fellow programmers” parody

Humor should arise from malfunctioning systems, dry AI dialogue, bureaucracy surviving the apocalypse, and odd remnants of colony life.

## 6.4 Main characters

### ORION

The original ship intelligence, now fragmented. Different ORION components may disagree.

### MICA

A maintenance drone with a damaged language model and an overly literal understanding of engineering policy. MICA acts as an optional hint companion.

### Dr. Sera Venn

Chief systems architect, represented through logs, code reviews, and recorded messages.

### Commander Hale

Mission commander whose decisions before transit appear increasingly suspicious.

### The Auditor

An unidentified security agent or service that blocks access, leaves signed warnings, and may be protecting the ship.

---

# 7. Ship Regions and Curriculum Map

| Region | Primary curriculum | Narrative system |
|---|---|---|
| Emergency Interface | Onboarding and computational thinking | Wake sequence and power routing |
| Maintenance Deck | Linux and Bash | Files, processes, permissions, repairs |
| Archive Core | SQL | Crew, cargo, and historical records |
| Version Vault | Git | Source history and unauthorized changes |
| Automation Lab | Python | Life-support and maintenance automation |
| Drone Foundry | OOP | Repair-drone software |
| Navigation Array | Data structures and algorithms | Routing and resource allocation |
| Communications Spire | APIs and backend fundamentals | Internal and external services |
| Verification Chamber | Testing | Safety certification and regression control |
| Black Vault | Defensive cybersecurity | Identity, permissions, secrets, and containment |
| Data Foundry | Data engineering | Telemetry, batch jobs, schemas, and lineage |
| Reactor Kernel | C++ | Performance-sensitive control modules |
| Prediction Observatory | ML infrastructure | Training, registry, deployment, and monitoring |
| Horizon Core | Integrated capstone | Multi-system production incident |

Domains may overlap. Later missions should require earlier skills.

---

# 8. Core Gameplay Loop

1. **Alert:** The player receives an incident, request, anomaly, or mystery.
2. **Briefing:** The game communicates the operational impact and technical objective.
3. **Inspection:** The player reads logs, browses files, examines tables, views tests, or traces a service map.
4. **Hypothesis:** The player decides what is likely wrong.
5. **Action:** The player runs a command, changes code, edits configuration, writes a query, creates a test, or changes system state.
6. **Execution:** The app evaluates the action locally or through the sandbox.
7. **Consequence:** The ship visibly responds.
8. **Debugging:** Failed attempts produce specific diagnostics.
9. **Verification:** Tests and mission objectives confirm the repair.
10. **Reward:** The player receives clearance, restored systems, lore, cosmetics, or new access.
11. **Reinforcement:** A later mission reuses the same concept with less guidance.

The game should treat failed attempts as investigation, not punishment.

---

# 9. Interaction Systems

## 9.1 Mission briefing

Each mission begins with:

- Incident title
- Affected system
- Operational stakes
- Primary objective
- Optional objectives
- Estimated conceptual difficulty: introductory, practiced, or challenge
- Tools available
- Relevant prior skills
- Accessibility option to disable time pressure

The game should not display an exact completion-time estimate to the player unless validated by analytics.

## 9.2 Simulated terminal

The terminal is a deterministic environment, not an operating-system shell.

Initial command support:

- `pwd`
- `ls`
- `cd`
- `cat`
- `less`
- `head`
- `tail`
- `grep`
- `find`
- `wc`
- `sort`
- `uniq`
- `cut`
- `mkdir`
- `touch`
- `cp`
- `mv`
- `rm`
- `echo`
- `chmod`
- `ps`
- `kill`
- `env`
- `export`
- Pipes
- Output redirection
- Basic globbing
- A constrained Bash-like script evaluator in later missions

The simulator must have:

- An explicit command registry
- A virtual filesystem scoped to the mission
- No access to Android storage outside the mission bundle
- Deterministic output
- Testable command semantics
- Reset and undo support where appropriate
- A parser that rejects unsupported syntax with clear feedback

## 9.3 Simulated Git

Git lessons should use a purpose-built repository-state simulator for the first release.

Supported concepts:

- Working tree
- Untracked, modified, staged, and committed states
- `status`
- `diff`
- `add`
- `commit`
- `log`
- `branch`
- `switch`
- `merge`
- Merge conflicts
- `.gitignore`
- Revert concepts
- Safe recovery from mistakes
- Pull-request and code-review concepts through narrative UI

The simulator should model the state transitions accurately enough to transfer to real Git, while avoiding an embedded native Git implementation in the first release.

Each Git mission has:

- A file tree
- Commit graph
- Current branch
- Index state
- Working-tree changes
- Expected state assertions

## 9.4 SQL console

SQL missions use a dedicated SQLite mission database stored separately from application data.

Requirements:

- One mission database per active mission instance
- Reset from immutable seed
- Allow `SELECT`, joins, aggregation, subqueries, common table expressions, inserts, updates, deletes, and transactions only when mission policy permits
- Reject `ATTACH`, unsafe pragmas, extension loading, and multi-statement payloads unless explicitly needed
- Never expose the Room application database to the learner query console
- Provide schema browser and sample-row view
- Provide query-plan visualization in advanced missions
- Compare result sets semantically when ordering is not required
- Support hidden test queries and state assertions

## 9.5 Mobile code editor

The first editor should be native and intentionally small.

Required:

- `BasicTextField`-based editor or equivalent Compose implementation
- Monospace font bundled through normal application resources
- Line numbers
- Horizontal scrolling
- Syntax token styling
- Undo and redo
- Find
- Auto-indent
- Bracket pairing
- A symbol toolbar for `()[]{}`, quotes, colon, underscore, pipe, slash, comparison operators, indentation, and arrows
- Portrait and landscape layouts
- Read-only starter files
- Editable target files
- Diff view
- Test-output panel
- Error location navigation

Do not build a full IDE in version 1.

Beginner missions may use:

- Fill-in-the-gap editing
- Reorderable code blocks
- Selectable snippets
- Guided insertion

Every chapter should eventually require some actual typing.

## 9.6 Test console

The test console displays:

- Test name
- Passed, failed, skipped, or error state
- Concise failure explanation
- Expected versus actual values when appropriate
- Stack trace collapsed by default
- Hidden-test count without exposing hidden assertions
- Regression indicator showing whether an earlier behavior broke

Players can run one test, a test file, or the full mission suite when supported.

## 9.7 Service map

Backend and ML-infrastructure missions use a visual service map.

Nodes can represent:

- API
- Database
- Cache
- Queue
- Batch job
- Object store
- Feature pipeline
- Model registry
- Inference service
- Monitoring service
- Client
- External system

Edges show:

- Request flow
- Data flow
- Authentication boundary
- Failure
- Latency
- Retry
- Queue backlog

The map is explanatory and interactive, not a full infrastructure simulator.

## 9.8 Hints

Hints are progressive:

1. Restate the objective in operational language.
2. Identify the relevant artifact.
3. Name the concept.
4. Suggest a command, query shape, test strategy, or algorithm family.
5. Show pseudocode.
6. Reveal a partial solution.
7. Show a complete worked explanation after repeated attempts.

Using hints does not block progress. Mastery scoring may distinguish assisted and unassisted completion.

## 9.9 Consequence presentation

Successful code should change the world:

- Lights return
- Pods stabilize
- Drones move
- Graphs normalize
- Network links reconnect
- Crew records repopulate
- Reactor temperature falls
- An AI voice becomes coherent
- A hidden region unlocks

Failure feedback should be equally concrete:

- A service restarts repeatedly
- A queue backlog grows
- A query returns duplicate allocations
- A unit test catches a regression
- A model rollback activates
- A permission denial blocks a process

---

# 10. Curriculum Architecture

## 10.1 Learning model

Every skill has four mastery states:

1. **Introduced:** The concept is explained and heavily scaffolded.
2. **Practiced:** The player uses it with partial guidance.
3. **Reliable:** The player solves a new problem with minimal guidance.
4. **Mastered:** The player applies it in an integrated or challenge incident.

A mission can tag multiple skills but should have one primary learning objective.

## 10.2 Retrieval practice

Previously learned skills should return after increasing intervals. Review missions must change the narrative context rather than repeat identical prompts.

## 10.3 Transfer

At least one mission in each chapter must require a concept learned in another chapter.

Examples:

- Use Linux tools to inspect data-pipeline logs.
- Use Git to identify when a security bug was introduced.
- Write a test before repairing an API.
- Query model-monitoring data with SQL.
- Use a hash map to optimize a Python service.
- Read C++ control code and add a regression test specification.

## 10.4 Explanation pattern

Every concept explanation follows:

1. Operational problem
2. Mental model
3. Minimal example
4. Player action
5. System feedback
6. Common mistake
7. Real-world transfer note

---

# 11. Initial Public Campaign

Version 1 targets approximately seventy-two core missions. Mission counts may shift during playtesting, but every domain must have a coherent arc.

## Chapter 0: Emergency Interface
**Six missions**

Skills:

- Reading system output
- Basic commands
- Variables and simple expressions
- First SQL query
- Running a test
- Understanding an incident objective

Capstone: Restore emergency power and open communication with ORION.

## Chapter 1: Maintenance Deck — Linux and Bash
**Six missions**

Skills:

- Navigation
- File inspection
- Search
- Pipes and redirection
- Processes
- Permissions and environment variables
- Simple scripts

Capstone: Find and stop a corrupted maintenance process without terminating life-support workers.

## Chapter 2: Version Vault — Git
**Six missions**

Skills:

- Working tree and staging
- Commits
- History
- Branches
- Merges
- Conflicts
- Ignore rules
- Revert and recovery concepts
- Code review

Capstone: Reconstruct an unauthorized change, preserve a valid repair, and merge it without restoring the sabotage.

## Chapter 3: Archive Core — SQL
**Seven missions**

Skills:

- Selection and filtering
- Sorting and limits
- Aggregation
- Grouping
- Joins
- Subqueries and CTEs
- Data modification
- Constraints
- Transactions
- Basic indexes

Capstone: Rebuild the passenger manifest and prove that twelve colonists were intentionally removed.

## Chapter 4: Automation Lab — Python
**Eight missions**

Skills:

- Values and types
- Conditions
- Loops
- Functions
- Collections
- Exceptions
- File processing
- Modules
- Basic classes
- Logging

Capstone: Automate oxygen allocation while handling missing and corrupt sensor readings.

## Chapter 5: Drone Foundry — Object-Oriented Programming
**Five missions**

Skills:

- Classes and instances
- Encapsulation
- Composition
- Interfaces and polymorphism
- Inheritance tradeoffs
- Refactoring responsibilities

Capstone: Replace a brittle drone hierarchy with composable navigation, repair, and safety modules.

## Chapter 6: Navigation Array — Data Structures and Algorithms
**Seven missions**

Skills:

- Lists and arrays
- Stacks and queues
- Sets and hash maps
- Searching and sorting
- Trees
- Graph traversal
- Big-O reasoning
- Recursion and iteration tradeoffs

Capstone: Find a safe path through a changing debris network within the navigation computer’s execution budget.

## Chapter 7: Communications Spire — APIs and Backend Fundamentals
**Five missions**

Skills:

- Client/server model
- HTTP methods
- Status codes
- JSON
- Validation
- Authentication concepts
- Error handling
- Idempotency
- Rate limiting
- Service dependencies

Capstone: Restore a communications API without duplicating distress transmissions or leaking restricted coordinates.

## Chapter 8: Verification Chamber — Testing
**Five missions**

Skills:

- Unit tests
- Arrange-act-assert
- Test doubles
- Integration tests
- Regression tests
- Boundary conditions
- Property-oriented thinking
- Coverage versus quality

Capstone: Certify a life-support patch by finding a hidden boundary defect and preventing its recurrence.

## Chapter 9: Black Vault — Defensive Cybersecurity
**Five missions**

Skills:

- Threat modeling
- Input validation
- SQL injection prevention
- Authentication versus authorization
- Least privilege
- Secret handling
- Secure logging
- Dependency and supply-chain awareness
- Safe error messages

Capstone: Contain an internal service account compromise without disabling critical ship systems.

Cybersecurity content must remain defensive and use fictional systems. No mission should provide deployable instructions for attacking real services.

## Chapter 10: Data Foundry — Data Engineering
**Five missions**

Skills:

- Batch pipelines
- Extract, transform, and load
- Schemas
- Data quality checks
- Idempotency
- Partitioning
- Orchestration
- Lineage
- Late-arriving data
- Batch versus streaming concepts

Capstone: Repair a telemetry pipeline that silently drops records and produces inconsistent reactor forecasts.

## Chapter 11: Reactor Kernel — C++
**Five missions**

Skills:

- Compilation mental model
- Types and functions
- References and const correctness
- Classes
- Standard containers
- Resource lifetime and RAII concepts
- Bounds safety
- Performance awareness
- Interoperability concept with Kotlin/JNI, shown but not required from the player

Capstone: Repair a performance-critical thermal-control module and eliminate a resource-lifetime defect.

## Chapter 12: Prediction Observatory — ML Infrastructure
**Five missions**

Skills:

- Training versus inference
- Dataset and feature versioning
- Reproducibility
- Batch and online features
- Model registry
- Deployment and rollback
- Monitoring
- Drift
- Data leakage
- Latency and reliability
- Human review and fallback

Capstone: Roll back a failing navigation-risk model, identify feature skew, and restore a reproducible training pipeline.

## Chapter 13: Horizon Core — Integrated Incident
**Two large missions**

Mission 1 combines Linux, Git, SQL, Python, testing, APIs, and security.

Mission 2 combines data engineering, C++, algorithms, and ML infrastructure.

The player discovers whether the hidden changes were sabotage or containment.

---

# 12. Example Integrated Mission

## Mission: Ghosts in the Registry

### Narrative

Cryogenic control reports twelve occupied pods with no valid passenger records. The allocation API refuses to deliver medical resources to unregistered occupants.

### Available tools

- SQL console
- Simulated terminal
- Git repository view
- Python editor
- Test console

### Stages

1. Query pod and passenger tables to identify unmatched records.
2. Search archived import files for the missing identifiers.
3. Inspect Git history to find the commit that changed validation behavior.
4. Write a regression test proving valid legacy identifiers must be accepted.
5. Repair the Python importer.
6. Run the importer in a mission sandbox.
7. Verify database state and API response.
8. Commit the repair in the simulated repository.

### Primary skills

- SQL joins
- Linux search
- Git history
- Python validation
- Regression testing

### Hidden twist

The records were rejected by a newly added pattern intended to exclude fabricated identities. The pattern was introduced in a signed commit by the chief security officer.

### Completion assertions

- Exactly twelve expected passengers are restored.
- No duplicate passenger IDs exist.
- Invalid control records remain rejected.
- All visible and hidden tests pass.
- The final simulated Git state contains a committed repair.
- The player does not modify protected source data directly.

---

# 13. Progression, Rewards, and Retention

## 13.1 Rank progression

1. Emergency Operator
2. Maintenance Technician
3. Systems Investigator
4. Automation Engineer
5. Backend Engineer
6. Reliability Engineer
7. Infrastructure Architect
8. Horizon Core Administrator

Rank is based on completed capabilities, not arbitrary grinding.

## 13.2 Skill map

Each skill node shows:

- Mastery state
- Missions completed
- Assisted versus unassisted success
- Last practiced
- Related concepts
- Suggested review

Do not use public numerical intelligence scores.

## 13.3 Rewards

- New ship regions
- Terminal themes
- Drone shells
- Cabin decorations
- Lore records
- Alternate system-map skins
- Sound packs
- Optional challenge incidents
- “Clean solution” badges for readable or efficient solutions
- Incident medals for optional constraints

No randomized paid rewards.

## 13.4 Daily systems

Version 1 may include:

- One short review incident
- One rotating diagnostic puzzle
- A personal practice streak
- Weekly integrated incident

Streak rules:

- Missing a day does not erase the streak permanently.
- Offer a limited “maintenance buffer” earned through play, not purchase.
- Do not send shame-based notifications.
- Notifications are opt-in and configurable.

## 13.5 Replayability

Missions can vary:

- Seed data
- File names
- Numeric values
- Record ordering
- Failure location
- Optional constraints

Variations must preserve learning validity and be deterministic from a mission seed.

---

# 14. Accessibility and Mobile Usability

Required for version 1:

- Scalable text
- Screen-reader labels
- Focus order
- High-contrast option
- Reduced-motion option
- Color-blind-safe status indicators
- No essential state represented by color alone
- External keyboard support
- Landscape editor mode
- Haptic feedback toggle
- Sound and music controls
- Untimed mode
- Ability to replay dialogue
- Plain-language error summaries
- Dyslexia-friendly spacing option if feasible after user testing
- Save-and-resume for long incidents

Touch targets must meet Android accessibility guidance.

---

# 15. Visual and Audio Direction

Authoritative token and component rules: [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) and [ADR-0021](ADR/0021-terminal-console-visual-language.md).

## 15.1 Art direction

**MU-TH-UR / terminal-console** aesthetic: a diegetic ship OS (NULL HORIZON OS), not a Material gallery and not neon cyberpunk overload.

Visual language:

- Near-black CRT backgrounds with phosphor text (white, green, amber, red, blue)
- Per-ship-region accent colors (for example amber Emergency, green Archive, red-shift Black Vault) always paired with textual status
- TUI panel chrome using box-drawing borders (`┌─ SYSTEMS ─┐`), not rounded Material cards
- Dense layout: tighter spacing, more information per screen, ALL-CAPS labels and headers
- Subtle CRT presentation: scanlines, vignette, optional curvature/glow/flicker — all disabled or flattened under reduced-motion and high-contrast accessibility prefs
- Strong monospace typography hierarchy
- Ship map as a technical schematic / region status board
- PC client may be more maximal (wider layouts, keybind hints); Android stays lean while sharing tokens and primitives

## 15.2 UI surfaces

- Boot / NULL HORIZON OS sequence
- Bridge / ship-map console
- Terminal
- Code editor
- Database browser
- Git graph
- Service map
- Test console
- Message/log / ORION-MICA dialogue viewer (typewriter reveal)
- Skill map
- Mission debrief

## 15.3 Motion moments

- Cold-start boot sequence (version line, memory/system checks, OK) with skip
- Typewriter reveal for ORION/MICA dialogue
- Blinking block cursor on text inputs
- Instant equivalents when reduced motion is enabled

## 15.4 Audio

- Minimal ambient score
- Mechanical feedback
- Distinct success tones
- Warning sounds that do not become exhausting
- Optional voice for major story beats only
- Full subtitles

---

# 16. System Architecture

## 16.1 High-level diagram

```text
┌──────────────────────────────────────────────────────────┐
│                    Android Application                   │
│                                                          │
│  Compose UI                                              │
│     │                                                    │
│  ViewModels / State Holders                              │
│     │                                                    │
│  Domain Use Cases                                        │
│     │                                                    │
│  Repositories                                            │
│   ├── Mission Content Repository                         │
│   ├── Progress Repository                                │
│   ├── Local Simulation Repository                        │
│   ├── Execution Repository ───────────────┐              │
│   └── Sync Repository                    │              │
│                                          │              │
│  Room / DataStore / Mission Bundles       │              │
└───────────────────────────────────────────┼──────────────┘
                                            │ HTTPS
                                            ▼
┌──────────────────────────────────────────────────────────┐
│                    FastAPI Application                   │
│                                                          │
│  Auth / Profiles / Content Manifest / Progress Sync      │
│  Execution Job API / Telemetry Ingestion                 │
│     │                                                    │
│  PostgreSQL          Redis                               │
│     │                 │                                  │
└─────┼─────────────────┼──────────────────────────────────┘
      │                 │ queued job reference
      │                 ▼
      │      ┌────────────────────────────────────────────┐
      │      │       Isolated Execution Orchestrator      │
      │      │                                            │
      │      │  Python Runner     C++ Runner              │
      │      │  No network        Read-only base image    │
      │      │  Strict limits     Ephemeral workspace     │
      │      └────────────────────────────────────────────┘
      │
      ▼
 Object storage for signed, versioned content bundles
```

## 16.2 Architectural boundaries

### Android UI layer

Responsible for:

- Rendering state
- Collecting player actions
- Accessibility
- Navigation
- Animation and sound triggers

Must not:

- Contain mission evaluation rules
- Directly access databases
- Build API payloads manually in composables
- Execute learner code

### Android domain layer

Responsible for:

- Mission state transitions
- Attempt submission
- Hint progression
- Reward calculation
- Mastery updates
- Local simulator orchestration
- Sync decisions

### Android data layer

Responsible for:

- Room entities
- Content bundle access
- API clients
- Execution polling
- Local settings
- Repository implementations

### Backend API

Responsible for:

- Optional identity
- Cloud progress sync
- Content manifests
- Signed content metadata
- Execution job submission
- Job status
- Server-side mission validation where required
- Telemetry intake
- Administrative content publication

Must not execute code in-process.

### Execution service

Responsible for:

- Creating ephemeral workspaces
- Materializing approved starter files
- Compiling or interpreting code
- Running tests
- Enforcing limits
- Capturing sanitized output
- Destroying the workspace
- Returning structured results

---

# 17. Android Application Architecture

## 17.1 Modules

```text
android-app/
├── app/
├── core/
│   ├── common/
│   ├── designsystem/
│   ├── model/
│   ├── database/
│   ├── datastore/
│   ├── network/
│   ├── content/
│   ├── simulation/
│   ├── execution/
│   └── testing/
├── feature/
│   ├── onboarding/
│   ├── shipmap/
│   ├── mission/
│   ├── terminal/
│   ├── git/
│   ├── sql/
│   ├── editor/
│   ├── tests/
│   ├── servicemap/
│   ├── skills/
│   ├── profile/
│   └── settings/
└── benchmark/
```

Begin with fewer Gradle modules if build complexity becomes a drag. Package boundaries are mandatory; physical modules can be introduced as features stabilize.

## 17.2 State model

Use unidirectional data flow.

```kotlin
data class MissionUiState(
    val mission: MissionViewData?,
    val phase: MissionPhase,
    val tools: List<ToolTab>,
    val activeTool: ToolTab,
    val objectives: List<ObjectiveUiState>,
    val attempts: List<AttemptSummary>,
    val hintLevel: Int,
    val execution: ExecutionUiState,
    val consequence: ConsequenceUiState?,
    val canSubmit: Boolean,
    val isLoading: Boolean,
    val error: UserFacingError?
)

sealed interface MissionAction {
    data object StartMission : MissionAction
    data class SelectTool(val tool: ToolTab) : MissionAction
    data class UpdateFile(val path: String, val content: String) : MissionAction
    data class RunCommand(val command: String) : MissionAction
    data class RunSql(val query: String) : MissionAction
    data object RunTests : MissionAction
    data object Submit : MissionAction
    data object RequestHint : MissionAction
    data object ResetMission : MissionAction
}
```

ViewModels expose immutable state and accept actions.

## 17.3 Local persistence

Use Room for:

- Player profile
- Mission progress
- Attempts
- Mastery
- Rewards
- Downloaded content metadata
- Pending sync operations
- Telemetry queue, if analytics is enabled

Use DataStore for:

- Theme
- Accessibility preferences
- Notification preferences
- Audio settings
- Analytics consent
- Last selected profile

Do not store tokens or secrets in plain DataStore. Use platform-backed secure storage for credentials.

## 17.4 Offline behavior

Offline-capable:

- Story
- Terminal simulator
- Git simulator
- SQL missions
- Config and architecture puzzles
- Previously downloaded content
- Progress
- Hints
- Review missions using local evaluators

Online required unless a later local runtime is added:

- General Python execution
- C++ compilation
- Some integrated tests
- Cloud sync
- Content downloads
- Telemetry upload

The UI must explicitly label online-required missions before the player starts.

## 17.5 Content bundle

The app ships with a starter bundle in assets. Additional bundles are downloaded, verified, and unpacked into app-private storage.

A content bundle contains:

```text
bundle/
├── manifest.json
├── chapters/
├── missions/
├── environments/
├── dialogue/
├── assets/
└── checksums.json
```

Every bundle has:

- Bundle ID
- Semantic version
- Minimum app version
- Content schema version
- File checksums
- Signature metadata
- Locale
- Release channel

---

# 18. Backend Architecture

## 18.1 Services

Initial deployment can be a modular monolith plus isolated runner workers.

Components:

1. **API service**
2. **PostgreSQL**
3. **Redis**
4. **Execution orchestrator**
5. **Python runner worker**
6. **C++ runner worker**
7. **Object storage**
8. **Administrative content publisher**, initially CLI-only

Avoid microservices until independent scaling or security boundaries justify them.

## 18.2 Backend package structure

```text
backend/
├── api/
│   ├── app/
│   │   ├── main.py
│   │   ├── core/
│   │   ├── auth/
│   │   ├── profiles/
│   │   ├── content/
│   │   ├── progress/
│   │   ├── execution/
│   │   ├── telemetry/
│   │   └── db/
│   └── tests/
├── runner/
│   ├── orchestrator/
│   ├── python_runner/
│   ├── cpp_runner/
│   ├── contracts/
│   └── tests/
└── shared/
    ├── schemas/
    └── observability/
```

## 18.3 API principles

- Version every public endpoint under `/v1`.
- Use Pydantic models.
- Generate OpenAPI.
- Use idempotency keys for execution submission and sync writes.
- Apply strict request-size limits.
- Use structured error responses.
- Never return raw internal stack traces.
- Use cursor pagination where lists can grow.
- Store timestamps in UTC.
- Keep server-generated IDs opaque.

## 18.4 Initial endpoints

```text
GET    /v1/health
GET    /v1/content/manifest
GET    /v1/content/bundles/{bundle_id}
POST   /v1/profiles/anonymous
POST   /v1/auth/exchange
GET    /v1/progress
PUT    /v1/progress/missions/{mission_id}
POST   /v1/progress/sync
POST   /v1/executions
GET    /v1/executions/{execution_id}
DELETE /v1/executions/{execution_id}
POST   /v1/telemetry/batch
```

Administrative endpoints should be separate, authenticated, and not included in the mobile client’s standard credentials.

---

# 19. Execution Sandbox

## 19.1 Threat statement

Learner-authored code is hostile input even when the player has good intentions. A syntax exercise can accidentally or intentionally attempt to:

- Read files
- Spawn processes
- Exhaust memory
- Exhaust CPU
- Fork repeatedly
- Access the network
- Probe metadata services
- Write excessive output
- Escape a container
- Read another player’s workspace
- Exploit compiler or runtime vulnerabilities

The runner is a security boundary.

## 19.2 Provider interface

```python
class ExecutionProvider(Protocol):
    async def submit(self, request: ExecutionRequest) -> ExecutionReceipt:
        ...

    async def get_result(self, execution_id: str) -> ExecutionResult:
        ...

    async def cancel(self, execution_id: str) -> None:
        ...
```

Implementations:

- `FakeExecutionProvider`: deterministic fixtures for UI development
- `LocalTrustedDockerProvider`: developer use only
- `HardenedSandboxProvider`: production
- Optional `ManagedSandboxProvider`: third-party service adapter

## 19.3 Production requirements

A production runner must provide:

- Isolation stronger than a normal shared application container
- No outbound network by default
- No inbound network
- Read-only base filesystem
- Ephemeral writable workspace
- Non-root user
- Dropped Linux capabilities
- Seccomp or equivalent syscall filtering
- Process-count limit
- CPU quota
- Memory limit
- Disk quota
- Output-size limit
- Wall-clock timeout
- Compile timeout
- Runtime timeout
- No mounted host secrets
- No Docker socket
- No cloud instance metadata access
- Unique workspace per execution
- Automatic destruction after result capture
- Dependency allowlist
- Pinned runtime and compiler images
- Image vulnerability scanning
- Audit logs without storing learner source longer than required

Docker-based isolation is acceptable for local trusted development. Public hostile code should use hardened container isolation, gVisor, microVMs, a managed code-execution platform, or an equivalent reviewed control.

## 19.4 Python execution

Allowed initial capabilities:

- Standard library allowlist
- Mission-provided files
- Pytest tests
- No package installation
- No network
- No subprocess creation
- No dynamic native extensions
- No unrestricted filesystem access

The runner returns:

```json
{
  "status": "completed",
  "compile_output": null,
  "stdout": "...",
  "stderr": "...",
  "tests": [
    {
      "id": "test_safe_valves",
      "status": "passed",
      "message": null
    }
  ],
  "metrics": {
    "duration_ms": 128,
    "peak_memory_kb": 14320
  },
  "truncated": false
}
```

## 19.5 C++ execution

- Use a pinned compiler image.
- Use a modern language standard selected by mission metadata.
- Compile with warnings enabled.
- Treat warnings as educational output; missions may decide whether warnings fail completion.
- Use sanitizers in selected missions.
- No networking.
- No external package installation.
- Provide mission-owned headers and tests.
- Enforce separate compile and run limits.
- Sanitize file paths from diagnostics before returning output.

## 19.6 Execution lifecycle

1. Validate request and mission version.
2. Resolve approved environment template.
3. Create execution record.
4. Queue job.
5. Create isolated workspace.
6. Materialize starter files and learner changes.
7. Compile if required.
8. Run visible and hidden tests.
9. Capture structured result.
10. Destroy workspace.
11. Persist result metadata.
12. Notify or allow client polling.
13. Delete source according to retention policy.

---

# 20. Local Simulators

## 20.1 Simulator contract

```kotlin
interface MissionSimulator<Input, State, Result> {
    fun initialize(environment: EnvironmentDefinition): State
    fun apply(state: State, input: Input): Result
    fun reset(environment: EnvironmentDefinition): State
    fun snapshot(state: State): ByteArray
    fun restore(snapshot: ByteArray): State
}
```

## 20.2 Terminal simulator

Core objects:

- `VirtualFile`
- `VirtualDirectory`
- `VirtualProcess`
- `VirtualUser`
- `PermissionSet`
- `EnvironmentVariables`
- `CommandParser`
- `CommandRegistry`
- `CommandResult`

Do not emulate every shell behavior. Implement documented supported behavior and reject the rest predictably.

## 20.3 Git simulator

Core objects:

- `Blob`
- `Tree`
- `Commit`
- `Branch`
- `Index`
- `WorkingTree`
- `RepositoryState`
- `MergeConflict`
- `GitCommand`

Commit hashes can be deterministic shortened hashes generated from content for narrative consistency.

## 20.4 Data-pipeline simulator

Represent a pipeline as a directed acyclic graph when possible.

Node types:

- Extract
- Transform
- Validate
- Load
- Partition
- Publish
- Train
- Register
- Deploy
- Monitor

State includes:

- Input datasets
- Schema versions
- Run timestamps
- Partitions
- Data-quality results
- Retry count
- Lineage edges
- Failure status

## 20.5 ML-infrastructure simulator

Objects:

- Dataset version
- Feature definition
- Feature materialization run
- Training run
- Experiment
- Model artifact
- Model version
- Registry stage
- Deployment
- Prediction log
- Monitoring metric
- Drift alert
- Rollback event

The simulator teaches operations around models. It is not a substitute for training large models.

---

# 21. Mission Content Model

## 21.1 Authoring format

Use YAML for human-authored mission definitions and compile them into validated JSON bundles.

Reasons:

- Reviewable in Git
- Friendly to writers and engineers
- Supports comments during authoring
- Easy schema validation
- Easy deterministic build step

## 21.2 Mission definition

```yaml
schema_version: 1
mission_id: archive.ghost_registry.01
version: 1.0.0
chapter_id: archive_core
title: Ghosts in the Registry
summary: Restore passenger records rejected by a damaged import service.
difficulty: practiced

requirements:
  app_version: ">=0.1.0"
  online: true
  prerequisite_skills:
    - sql.join.inner
    - linux.grep
    - git.log
    - python.functions
    - testing.regression

skills:
  primary: testing.regression
  secondary:
    - sql.join.left
    - git.history
    - python.validation

narrative:
  briefing_dialogue_id: dialogue.archive.ghost_registry.briefing
  success_dialogue_id: dialogue.archive.ghost_registry.success
  failure_consequence_id: consequence.cryo_allocation_blocked

tools:
  - terminal
  - sql
  - git
  - python_editor
  - test_console

environment:
  template_id: integrated.python_sql.v1
  seed: 481516
  files:
    - source: environments/ghost_registry/importer.py
      target: /workspace/importer.py
      editable: true
    - source: environments/ghost_registry/test_importer.py
      target: /workspace/test_importer.py
      editable: false
  databases:
    - id: registry
      seed_file: environments/ghost_registry/registry.sqlite

objectives:
  - id: identify_missing
    type: sql_result
    description: Identify all occupied pods without valid passenger records.
    visible: true
  - id: add_regression_test
    type: file_assertion
    description: Add a regression test for legacy identifiers.
    visible: true
  - id: repair_importer
    type: execution_tests
    description: Repair the importer without accepting invalid control records.
    visible: true
  - id: preserve_integrity
    type: database_assertion
    description: Preserve uniqueness and existing valid records.
    visible: false

hints:
  - level: 1
    text: Compare occupied pods against passenger assignments.
  - level: 2
    text: A LEFT JOIN can preserve pods that have no matching passenger.
  - level: 3
    text: Inspect the commit that changed the identifier pattern.
  - level: 4
    pseudocode: |
      expected_missing = ...
      assert import_record(legacy_record) is accepted

rewards:
  clearance_points: 100
  mastery:
    testing.regression: 1
    sql.join.left: 1
  unlocks:
    - lore.security_officer.03

completion:
  mode: all
  objective_ids:
    - identify_missing
    - add_regression_test
    - repair_importer
    - preserve_integrity
```

## 21.3 Objective types

Initial objective types:

- `command_output`
- `filesystem_state`
- `process_state`
- `git_state`
- `sql_result`
- `database_assertion`
- `file_assertion`
- `execution_tests`
- `service_map_state`
- `pipeline_state`
- `mlops_state`
- `dialogue_choice`
- `composite`

## 21.4 Mission validation

The content build must fail when:

- IDs are duplicated.
- References are missing.
- A prerequisite skill does not exist.
- An objective type lacks required fields.
- An editable file path escapes the workspace.
- A hidden objective leaks its expected answer.
- Hint levels are not sequential.
- An online mission lacks an execution environment.
- A mission has no primary skill.
- A chapter has unreachable prerequisites.
- A localized dialogue key is missing.
- A checksum differs from the content manifest.
- Seeded simulator output is nondeterministic.

---

# 22. Data Model

## 22.1 Android entities

### PlayerProfileEntity

- `profileId`
- `displayName`
- `createdAt`
- `rank`
- `clearancePoints`
- `cloudAccountId`, nullable

### MissionProgressEntity

- `profileId`
- `missionId`
- `missionVersion`
- `status`
- `bestAssistanceLevel`
- `attemptCount`
- `completedAt`
- `lastPlayedAt`
- `contentSnapshotVersion`

### SkillMasteryEntity

- `profileId`
- `skillId`
- `masteryLevel`
- `evidenceCount`
- `unassistedEvidenceCount`
- `lastPracticedAt`

### AttemptEntity

- `attemptId`
- `profileId`
- `missionId`
- `startedAt`
- `submittedAt`
- `result`
- `hintLevelUsed`
- `executionId`, nullable
- `localSnapshotPath`, nullable

### RewardEntity

- `profileId`
- `rewardId`
- `unlockedAt`
- `equipped`

### PendingSyncEntity

- `operationId`
- `operationType`
- `payload`
- `createdAt`
- `attemptCount`
- `lastError`

## 22.2 Server tables

- `accounts`
- `profiles`
- `devices`
- `progress_snapshots`
- `mission_completions`
- `skill_evidence`
- `execution_jobs`
- `execution_results`
- `content_bundles`
- `content_releases`
- `telemetry_batches`
- `audit_events`

Do not store full learner source indefinitely by default.

---

# 23. Progress Sync

## 23.1 Local-first rule

The local profile is authoritative while offline.

## 23.2 Merge rules

- Mission completion is monotonic.
- Best assistance level keeps the least-assisted successful completion.
- Attempt count is additive only when operation IDs are unique.
- Rewards are set union.
- Mastery evidence is event-based and deduplicated.
- Settings remain device-local unless explicitly marked syncable.
- Conflicting content versions retain both evidence records but display the newest supported mission version.

## 23.3 Privacy

Cloud sync is optional. The player can delete cloud progress and continue locally.

---

# 24. API Contracts

## 24.1 Submit execution

```json
POST /v1/executions
{
  "idempotency_key": "3a8819d2-...",
  "mission_id": "automation.oxygen_allocator.04",
  "mission_version": "1.0.0",
  "runtime": "python",
  "environment_id": "python.pytest.v1",
  "files": [
    {
      "path": "oxygen.py",
      "content": "def allocate(...):\n    ..."
    }
  ]
}
```

Response:

```json
{
  "execution_id": "exe_01H...",
  "status": "queued",
  "poll_after_ms": 750
}
```

## 24.2 Execution result

```json
{
  "execution_id": "exe_01H...",
  "status": "completed",
  "outcome": "failed_tests",
  "stdout": "",
  "stderr": "",
  "diagnostics": [
    {
      "severity": "error",
      "file": "oxygen.py",
      "line": 12,
      "column": 9,
      "message": "TypeError: unsupported operand..."
    }
  ],
  "tests": [
    {
      "id": "visible.normal_readings",
      "visibility": "visible",
      "status": "passed",
      "message": null
    },
    {
      "id": "hidden.missing_sensor",
      "visibility": "hidden",
      "status": "failed",
      "message": "The allocator must handle a missing sensor reading."
    }
  ],
  "metrics": {
    "compile_ms": 0,
    "run_ms": 83,
    "peak_memory_kb": 11800
  },
  "truncated": false
}
```

## 24.3 Error shape

```json
{
  "error": {
    "code": "MISSION_VERSION_UNSUPPORTED",
    "message": "This mission version is no longer accepted by the execution service.",
    "request_id": "req_01H..."
  }
}
```

---

# 25. Security Specification

## 25.1 Security principles

- Minimize collected data.
- Default to local play.
- Treat all client input as untrusted.
- Treat learner code as hostile.
- Keep secrets out of the repository.
- Use least privilege.
- Separate application and execution trust boundaries.
- Make defensive security concepts visible inside the game.
- Log security events without logging sensitive payloads.
- Make destructive administrative actions auditable.

## 25.2 Threat model

Assets:

- Player account and progress
- Content signing keys
- Execution infrastructure
- API credentials
- Administrative publishing access
- Other players’ code and data
- Mission answer integrity
- Application signing keys
- Telemetry data

Threats:

- Account takeover
- Token theft
- API abuse
- Runner escape
- Denial of service through code
- Content-bundle tampering
- Mission-answer extraction
- Replay of progress updates
- Dependency compromise
- Secret leakage
- Insecure local storage
- Malicious or accidental administrative publication

## 25.3 Required controls

### Mobile

- HTTPS only
- Network security configuration that blocks cleartext traffic
- Secure credential storage
- No embedded backend secrets
- No logging of auth tokens
- Content checksum and signature verification
- App-private mission storage
- Exported Android components disabled unless required
- Minimal permissions
- No broad filesystem permission
- Dependency scanning
- Release builds with debug features removed

### API

- Authentication for synced profiles and execution jobs
- Authorization checks on every profile-bound resource
- Rate limits by account, device, and IP risk signal
- Request-size limits
- Schema validation
- Parameterized database access
- Secure password hashing only if passwords are supported
- Rotatable keys
- Short-lived access tokens
- Audit logging
- CORS restricted to actual administrative web origins
- Secure headers
- Dependency pinning and scanning
- Database credentials scoped to service needs

### Runner

Follow all controls in Section 19.

### Content pipeline

- Signed release manifests
- Review requirement for executable environment changes
- Mission content cannot add arbitrary runner images
- Runner images selected from an allowlist
- Hidden tests stored server-side for online execution
- Content publication separated from code deployment
- Rollback support

## 25.4 Defensive cybersecurity curriculum boundaries

Allowed:

- Fixing injection vulnerabilities in fictional code
- Correct use of parameterized queries
- Least-privilege exercises
- Secure secret handling
- Authentication and authorization distinctions
- Log redaction
- Dependency review
- Threat modeling
- Secure input validation
- Incident containment

Disallowed in content:

- Targeting real organizations
- Credential theft workflows
- Persistence techniques
- Malware development
- Stealth or evasion training
- Real exploit chains
- Instructions that materially enable unauthorized access

---

# 26. Testing Strategy

## 26.1 Test pyramid

### Android unit tests

- Mission reducers
- Use cases
- Mastery calculations
- Sync merge rules
- Terminal parser
- Git state transitions
- SQL result comparison
- Pipeline simulator
- Content parsing

### Android integration tests

- Room migrations
- Content bundle installation
- Repository behavior
- API serialization
- Execution polling
- Offline-to-online sync

### Compose UI tests

- Onboarding
- Mission start
- Tool switching
- Terminal input
- Code editing
- Hint flow
- Accessibility labels
- Completion flow
- Rotation and state restoration

### Backend unit tests

- API services
- Authorization
- Progress merge
- Execution validation
- Content manifest logic
- Retention rules

### Backend integration tests

- PostgreSQL
- Redis queue
- Object storage adapter
- Runner contract
- Idempotency
- Rate limiting

### Runner security tests

- Timeout
- Memory exhaustion
- Process spawning
- Network denial
- Filesystem isolation
- Output flooding
- Path traversal
- Cross-job isolation
- Cancellation
- Workspace destruction

### Content tests

- Schema validation
- Reference validation
- Determinism
- Objective reachability
- Hint completeness
- Seeded expected outputs
- Hidden-test integrity
- Prerequisite graph

## 26.2 Quality gates

A pull request cannot merge when:

- Formatting or lint fails.
- Unit tests fail.
- Content validation fails.
- API schema compatibility check fails.
- Secrets are detected.
- A high-severity known dependency issue lacks an approved exception.
- Database migrations are missing for model changes.
- A new mission lacks learning objectives and completion assertions.
- A runner change lacks security-focused tests.

## 26.3 Coverage

Do not use code coverage as the only quality metric. Establish practical minimums for critical domain and security code, while requiring behavior-focused tests for all new features.

---

# 27. Observability and Analytics

## 27.1 Product analytics

Collect only with consent where required.

Events:

- App opened
- Chapter viewed
- Mission started
- Mission resumed
- Tool opened
- Attempt submitted
- Test run
- Hint requested
- Mission completed
- Mission abandoned
- Content downloaded
- Execution failed by category
- Accessibility feature enabled

Avoid collecting raw code in product analytics.

## 27.2 Learning analytics

Derived metrics:

- Tutorial completion
- Chapter completion
- Attempts before success
- Hint level before success
- Assisted versus unassisted completion
- Skill retention in later missions
- Failure categories
- Review mission performance
- Time between introduction and reliable use

## 27.3 Operational observability

- Structured JSON logs
- Request IDs
- Execution IDs
- Metrics for queue depth, job duration, timeout rate, failure rate, and resource use
- Traces across API and runner boundaries
- Alerts for abnormal execution escape indicators, job spikes, signature failures, and error-rate changes

Do not log access tokens, full learner code, or hidden-test contents.

---

# 28. Content Authoring Workflow

1. Write mission concept and learning objective.
2. Create mission YAML.
3. Add environment fixtures.
4. Add visible tests.
5. Add hidden tests for online missions.
6. Add progressive hints.
7. Add narrative dialogue.
8. Run schema validation.
9. Run deterministic simulator tests.
10. Run playtest in developer mode.
11. Review for pedagogy.
12. Review for security boundaries.
13. Merge through pull request.
14. Build signed content bundle.
15. Publish to staging.
16. Promote to production after validation.

Each mission pull request should include:

- Skill objective
- Expected player mental model
- Common misconceptions
- Solution outline
- Test strategy
- Accessibility notes
- Narrative purpose

---

# 29. Repository Structure

```text
null-horizon/
├── AGENTS.md
├── README.md
├── LICENSE
├── SECURITY.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── .editorconfig
├── .gitignore
├── .github/
│   ├── workflows/
│   ├── ISSUE_TEMPLATE/
│   ├── pull_request_template.md
│   ├── dependabot.yml
│   └── CODEOWNERS
├── android-app/
│   ├── app/
│   ├── core/
│   ├── feature/
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── backend/
│   ├── api/
│   ├── runner/
│   ├── shared/
│   ├── pyproject.toml
│   └── alembic.ini
├── content/
│   ├── schema/
│   ├── skills/
│   ├── chapters/
│   ├── missions/
│   ├── environments/
│   ├── dialogue/
│   └── tools/
├── shared/
│   ├── openapi/
│   ├── jsonschema/
│   └── fixtures/
├── infra/
│   ├── compose/
│   ├── terraform/
│   ├── policies/
│   └── images/
├── scripts/
│   ├── bootstrap.sh
│   ├── validate_content.py
│   ├── build_bundle.py
│   ├── generate_client.sh
│   └── check.sh
└── docs/
    ├── PRODUCT_SPEC.md
    ├── ARCHITECTURE.md
    ├── THREAT_MODEL.md
    ├── CONTENT_AUTHORING.md
    ├── CURRICULUM.md
    ├── API.md
    ├── ADR/
    └── playtests/
```

---

# 30. Local Development

## 30.1 Prerequisites

- Current stable Android Studio
- Supported JDK for the selected Android Gradle Plugin
- Android SDK and emulator
- Python 3.12 or later unless dependency constraints require adjustment
- Docker and Docker Compose for trusted local backend development
- PostgreSQL client tools
- Git
- Make or a cross-platform task runner

Exact tool versions must be pinned in the repository and updated intentionally.

## 30.2 Bootstrap commands

Target developer experience:

```bash
git clone <repository>
cd null-horizon
./scripts/bootstrap.sh
./scripts/check.sh
```

Backend:

```bash
docker compose -f infra/compose/dev.yml up --build
```

Android:

```bash
cd android-app
./gradlew test
./gradlew connectedCheck
./gradlew assembleDebug
```

Content:

```bash
python scripts/validate_content.py
python scripts/build_bundle.py --channel dev
```

## 30.3 Environment variables

Use `.env.example` with names only. Never commit real values.

Expected variables:

```text
DATABASE_URL
REDIS_URL
OBJECT_STORAGE_ENDPOINT
OBJECT_STORAGE_BUCKET
CONTENT_SIGNING_PUBLIC_KEY
CONTENT_SIGNING_PRIVATE_KEY_PATH
JWT_ISSUER
JWT_AUDIENCE
JWT_PUBLIC_KEY_PATH
JWT_PRIVATE_KEY_PATH
TELEMETRY_ENABLED
EXECUTION_PROVIDER
```

Private keys belong in local secure storage or deployment secret management, not repository files.

---

# 31. CI/CD

## 31.1 Pull-request workflow

Run:

- Kotlin formatting
- Android lint
- Android unit tests
- Backend formatting and lint
- Type checking
- Backend tests
- Content validation
- JSON Schema validation
- OpenAPI compatibility check
- Secret scanning
- Dependency scanning
- Containerfile lint
- Runner security smoke tests
- Debug Android build

## 31.2 Main-branch workflow

Additionally:

- Build signed internal Android artifact
- Build backend images
- Generate SBOM
- Scan images
- Publish staging content bundle
- Run integration suite
- Deploy to staging with approval rules

## 31.3 Release workflow

- Manual release approval
- Version tag
- Reproducible content bundle
- Signed Android App Bundle
- Backend image promotion by immutable digest
- Database migration check
- Rollback plan
- Release notes
- Privacy and policy checklist
- Google Play pre-launch report review

The agent must not configure production credentials or publish to Google Play without explicit human approval.

---

# 32. Delivery Epics

## Epic 0: Repository and standards

Deliver:

- Monorepo
- License decision placeholder
- README
- AGENTS.md
- Security policy
- Contribution guide
- CI skeleton
- Formatting and lint
- ADR template
- Issue templates

Acceptance:

- A clean clone can run validation scripts.
- CI executes against a trivial Android and backend project.
- No secrets exist in history.

## Epic 1: Android application shell

Deliver:

- Compose app
- Navigation
- Design tokens
- Theme
- Accessibility settings
- Local profile
- Ship-map placeholder
- Mission list
- Settings

Acceptance:

- App launches on supported emulator.
- State survives rotation.
- Accessibility labels exist for primary navigation.
- No account is required.

## Epic 2: Mission content engine

Deliver:

- Mission schema
- YAML parser/build step
- Starter content bundle
- Validation CLI
- Local content repository
- Mission state machine
- Objective engine
- Hint engine

Acceptance:

- Invalid content fails at build time.
- A valid mission renders from data without hardcoded mission UI.
- Mission reset is deterministic.

## Epic 3: Simulated terminal

Deliver:

- Virtual filesystem
- Command parser
- Initial commands
- Terminal UI
- History
- Objective assertions

Acceptance:

- Commands produce deterministic output.
- No Android filesystem is accessible.
- Unsupported syntax yields clear errors.
- Unit tests cover command behavior.

## Epic 4: Simulated Git

Deliver:

- Repository state model
- Command handling
- Commit graph UI
- Conflict UI
- Objective assertions

Acceptance:

- Working tree, staging, commit, branch, and merge flows behave consistently.
- A merge-conflict mission can be completed and reset.
- State transitions have unit tests.

## Epic 5: SQL console

Deliver:

- Mission database installer
- Query editor
- Schema browser
- Safe query execution
- Result table
- State assertions

Acceptance:

- Learner queries cannot access application tables.
- Mission database resets to seed.
- Result comparisons support ordered and unordered objectives.
- Unsafe statements are blocked by policy.

## Epic 6: Code editor and test console

Deliver:

- Mobile editor
- File tabs
- Symbol toolbar
- Diff view
- Test-result model
- Fake execution provider

Acceptance:

- Player can edit starter code and submit it.
- Fake results drive complete success and failure flows.
- Editor state survives navigation and rotation.

## Epic 7: Backend foundation

Deliver:

- FastAPI app
- PostgreSQL models
- Alembic
- Redis adapter
- Health endpoint
- Content manifest
- Anonymous profile
- Progress sync
- Execution job API using fake provider

Acceptance:

- OpenAPI generated.
- Authorization tests exist.
- Idempotency works for writes.
- Local Docker Compose starts all trusted development services.

## Epic 8: Execution orchestration

Deliver:

- Execution contracts
- Queue
- Python worker
- C++ worker
- Limits
- Result normalization
- Cancellation
- Retention cleanup

Acceptance:

- Code never runs in API process.
- Timeout, memory, network, process, and output tests pass.
- Jobs cannot see one another’s workspace.
- Public deployment remains blocked until security review.

## Epic 9: Progression and rewards

Deliver:

- Skill graph
- Mastery evidence
- Rank
- Rewards
- Debrief
- Review recommendations

Acceptance:

- Completion updates mastery.
- Assisted completion is tracked.
- Rewards are deterministic and idempotent.
- Offline changes sync without losing completion.

## Epic 10: Service-map, pipeline, and ML-infrastructure simulators

Deliver:

- Graph model
- Node editor or controlled action UI
- Pipeline run state
- ML lifecycle state
- Objective assertions
- Visual failure states

Acceptance:

- Data-engineering and ML-infrastructure missions can run without real cloud resources.
- Simulations are seeded and deterministic.
- State changes are explained to the player.

## Epic 11: Vertical-slice content

Deliver twelve polished missions:

1. Wake sequence
2. Find a system file
3. Stop a bad process
4. Inspect Git status
5. Recover a safe commit
6. Query missing crew
7. Repair a Python function
8. Write a regression test
9. Trace an API failure
10. Fix an unsafe query
11. Repair a pipeline
12. Integrated incident

Acceptance:

- All missions meet content standards.
- A new player can complete the slice.
- At least one mission uses online execution and has an offline explanatory fallback.
- Playtest findings are recorded.

## Epic 12: Public-version curriculum

Deliver the complete chapter structure in Section 11.

Acceptance:

- Every required domain has an introduction, practice, and capstone.
- Prerequisite graph is valid.
- Every mission has tested hints.
- Integrated incidents reuse earlier skills.
- Defensive-security review passes.

## Epic 13: Release readiness

Deliver:

- Privacy policy
- Data-deletion path
- Store assets
- Crash reporting with privacy review
- Performance profiling
- Accessibility audit
- Security assessment
- Content-rating review
- Closed test track
- Rollback process

Acceptance:

- No unresolved critical security findings.
- Core opening campaign works without account creation.
- Player data can be exported or deleted as promised.
- Store listing accurately describes online code execution and data use.

---

# 33. Vertical Slice Specification

The vertical slice should be the first complete playable target.

## 33.1 Required screens

- Boot screen
- Local profile creation
- Ship map
- Mission briefing
- Dialogue
- Terminal
- Git view
- SQL console
- Code editor
- Test console
- Service map
- Hint panel
- Mission debrief
- Skill map
- Settings

## 33.2 Required backend behavior

- Anonymous profile
- Content manifest
- Progress sync
- Fake execution
- One real Python execution path in controlled staging
- Structured execution result

## 33.3 Required narrative arc

The player wakes, restores a maintenance subsystem, discovers an unsigned change, repairs a crew-registry defect, and receives a message from the Auditor warning them to stop.

## 33.4 Slice exit criteria

- Twelve missions can be played in sequence.
- Offline missions work in airplane mode.
- Progress survives app restart.
- Mission content is loaded from bundles.
- At least one failed attempt produces a specific, useful explanation.
- At least one mission integrates three tools.
- Accessibility basics pass manual review.
- Runner security gates pass in staging.
- Content can be added without changing mission-screen code.

---

# 34. Definition of Done

A feature is done only when:

- Acceptance criteria are met.
- Unit or integration tests exist.
- Accessibility is considered.
- User-facing errors are written.
- Analytics behavior is documented.
- Security impact is reviewed.
- No secrets are added.
- Documentation is updated.
- Content changes pass schema validation.
- The feature works offline when specified.
- Loading, empty, error, and retry states exist.
- The pull request is small enough to review meaningfully.
- There is no known critical defect.

A mission is done only when:

- Learning objective is explicit.
- Narrative purpose is explicit.
- Completion assertions are deterministic.
- Visible and hidden tests are appropriate.
- Hints are progressive.
- Reset works.
- Common misconceptions are addressed.
- Mobile typing burden is acceptable.
- Accessibility notes are implemented.
- A human playtest has been recorded.

---

# 35. Agentic Coding Operating Rules

The coding agent must follow these rules.

## 35.1 General behavior

1. Read `AGENTS.md`, this specification, relevant ADRs, and nearby tests before changing code.
2. Do not invent a different architecture without proposing an ADR.
3. Work in small, reviewable increments.
4. State assumptions in the pull-request description.
5. Prefer the simplest implementation that preserves the specified interfaces.
6. Do not add dependencies without documenting why.
7. Do not silently weaken tests.
8. Do not replace deterministic simulation with an LLM.
9. Do not hardcode mission-specific logic into generic UI.
10. Keep learning content separate from platform code.

## 35.2 Security behavior

1. Never commit secrets.
2. Never print tokens.
3. Never mount the Docker socket into a runner.
4. Never execute learner code in the API service.
5. Never grant privileged container access.
6. Never enable network for learner code without an approved ADR.
7. Never use production credentials in tests.
8. Never publish to stores or production without explicit human approval.
9. Flag any request that weakens isolation.
10. Add security tests with runner changes.

## 35.3 Git behavior

1. Do not push directly to the protected main branch.
2. Use focused branches.
3. Make coherent commits.
4. Do not rewrite shared history.
5. Do not remove unrelated work.
6. Do not combine formatting of the entire repository with a feature change.
7. Include migrations and generated schemas when required.
8. Keep generated artifacts clearly identified.

## 35.4 Quality behavior

Before marking a task complete, run the closest equivalent of:

```bash
./scripts/check.sh
```

Report:

- Files changed
- Tests run
- Tests not run and why
- Security implications
- Follow-up work
- Any deviation from the specification

---

# 36. First Implementation Tasks

Complete these in order unless a blocking dependency requires a change.

## Task 1: Initialize repository

Create the monorepo structure, root documentation, editor configuration, ignore files, and CI placeholders.

## Task 2: Create Android shell

Create the Compose application with navigation, theme, local profile, ship map placeholder, mission list placeholder, and settings.

## Task 3: Define shared content schemas

Create JSON Schema for:

- Skill
- Chapter
- Mission
- Environment
- Dialogue
- Reward
- Content manifest

Add validation tooling and fixtures.

## Task 4: Implement one data-driven mission

Implement “Emergency Lighting” using only local actions and no specialized terminal.

Prove:

- YAML compiles
- Android loads content
- Objectives update
- Hint works
- Completion persists

## Task 5: Implement terminal simulator

Support `pwd`, `ls`, `cd`, `cat`, and `grep`.

Create “Locate the Fault Log.”

## Task 6: Implement Git simulator foundation

Support `status`, `diff`, `add`, `commit`, and `log`.

Create “Unsigned Change.”

## Task 7: Implement SQL console foundation

Seed a mission SQLite database and support safe `SELECT`.

Create “Missing Crew.”

## Task 8: Implement editor and fake execution

Create Python editor and test console driven by fixtures.

Create “Pressure Threshold.”

## Task 9: Bootstrap backend

Implement health, content manifest, anonymous profile, progress sync, and fake execution endpoints.

## Task 10: Connect Android execution repository

Submit fake execution and render structured tests and diagnostics.

## Task 11: Create real runner proof

Implement a trusted-development Python worker behind the same interface. Do not expose publicly.

## Task 12: Complete vertical-slice integrated incident

Build “Ghosts in the Registry.”

---

# 37. Architecture Decision Records Required Early

Create ADRs for:

1. Kotlin and Compose Android client
2. Monorepo
3. Local-first profile and optional sync
4. YAML authoring compiled to JSON bundles
5. Simulated terminal instead of embedded shell
6. Simulated Git instead of native Git library in version 1
7. Local SQLite mission databases
8. Remote Python and C++ execution
9. Production sandbox technology
10. Content signing
11. Analytics and consent
12. Authentication provider
13. Object storage provider
14. Minimum supported Android version
15. Open-source license

---

# 38. Performance Requirements

Initial targets:

- Cold start should feel responsive on a mid-range supported Android device.
- Ship map and mission list should not wait on network when local content exists.
- Terminal commands should normally return immediately.
- SQL query results should be paginated in the UI.
- Editor input must remain responsive for mission-sized files.
- Mission state should save after meaningful actions.
- Execution polling should use backoff and stop when the screen is backgrounded.
- Content bundles should be compressed and incremental where practical.
- Large logs should be truncated safely with an option to inspect available segments.

Performance targets must be measured and refined through profiling rather than assumed.

---

# 39. Privacy Requirements

Default collected data should be minimal.

Local-only player:

- No email required
- No real name required
- Progress stored on device
- Analytics opt-out available
- No raw learner code sent except when the player invokes online execution

Cloud-sync player:

- Account identifier
- Progress
- Rewards
- Skill evidence
- Device sync metadata

Execution:

- Source transmitted only for execution
- Clear disclosure before first online run
- Short retention by default
- No use of learner code for model training without explicit separate consent
- No sale of personal data
- Deletion path

Telemetry:

- No raw source
- No terminal history payloads
- No SQL text unless specifically sampled through an explicit diagnostic-consent flow
- No secrets
- Coarse device and performance data only as necessary

---

# 40. Monetization Guardrails

Allowed:

- Optional supporter purchase
- Cosmetic themes
- Cosmetic drone skins
- Additional story campaigns after the complete foundational campaign
- Classroom or organization licenses
- Donations
- Optional ad-free support tier if ads are ever introduced

Not allowed:

- Charging for hints required to progress
- Selling correct answers
- Energy refills
- Randomized paid loot
- Paying to repair a streak
- Pay-to-win leaderboards
- Restricting SQL, Python, Git, testing, security, data-engineering, C++, or ML-infrastructure foundations behind payment

---

# 41. Open Questions

These do not block repository initialization but require product decisions before public release:

- Final title and brand clearance
- Open-source license
- Authentication provider
- Production sandbox provider
- Cloud provider
- Content-signing key custody
- Exact minimum Android version
- Account age policy
- Whether optional ads exist
- Whether advanced story campaigns are paid
- Voice acting scope
- Localization priority
- Classroom roadmap
- Public source-code visibility for hidden tests
- Whether offline Python execution becomes a later feature

Record each resolved question in an ADR or product-decision log.

---

# 42. Reference Baseline

Implementation should align with current official guidance at the time dependencies are pinned, especially:

- Android Developers: Guide to app architecture
- Android Developers: Jetpack Compose documentation
- Android Developers: Android NDK and C++ integration guidance
- FastAPI: Security and deployment documentation
- Docker: Engine security, rootless mode, and seccomp guidance

Official guidance changes. Dependency versions and platform requirements must be checked during bootstrap and release work rather than copied indefinitely from this document.

---

# 43. Final Product Standard

NULL HORIZON should not feel like a course with a spaceship background.

It should feel like a spaceship that can only be understood, repaired, and ultimately saved by learning how real software systems work.

The design is successful when a player says:

> “I came back to find out what the Auditor was hiding, and somewhere along the way I learned how tests, Git, pipelines, APIs, and model deployments actually fit together.”
