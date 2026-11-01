# Cybertron Recomp Labs

Research project exploring the feasibility of running **Transformers: War for Cybertron (Xbox 360)** on native Android ARM64 through static recompilation.

> **Status: Early research / proof-of-concept planning.** This repository does not contain a working port, and a successful port is not guaranteed.

## Goals

- Document the Xbox 360 executable format and PowerPC code-analysis workflow.
- Investigate whether static recompilation to ARM64 is technically feasible.
- Build small, independently testable research tools and experiments.
- Track compatibility gaps, blockers, and findings transparently.
- Keep the workflow accessible to contributors using browser-based development environments where possible.

## Technical challenges

A complete port would likely need to address:

- Xbox 360 XEX/PE executable analysis and PowerPC instruction translation.
- Operating-system and game-runtime APIs.
- Memory layout, threads, synchronization, and timing.
- Xbox 360 Xenos graphics, shader translation, and rendering.
- Audio, input, file access, and Android integration.
- Testing and performance on real ARM64 devices.

Static recompilation alone does not automatically solve these platform-specific dependencies.

## Planned milestones

- [ ] **M0 — Project setup:** document scope, constraints, and research questions.
- [ ] **M1 — Executable identification:** record the file type and basic metadata of a legally obtained local copy; do not commit the game executable.
- [ ] **M2 — Format research:** document public references for XEX/PE structure and PowerPC basics.
- [ ] **M3 — Minimal experiments:** create small tests using original or synthetic input data.
- [ ] **M4 — Recompilation feasibility:** evaluate a limited instruction subset and document unsupported instructions.
- [ ] **M5 — Runtime feasibility:** investigate system APIs and graphics dependencies before attempting game execution.

Milestones will be marked complete only when results and reproducible evidence are documented.

## Repository structure

The project will grow incrementally. Suggested areas:

- `docs/` — research notes, architecture, and decision records.
- `tools/` — small analysis utilities that do not redistribute game content.
- `experiments/` — isolated tests using synthetic or original test data.
- `tests/` — automated checks for project-owned code.

## Legal and safety boundaries

- Do not commit or publish game executables, copyrighted assets, console firmware, encryption keys, or proprietary game data.
- Use legally obtained copies and work only with files you are authorized to inspect.
- Prefer public documentation and original test fixtures.
- This is an independent research project and is not affiliated with or endorsed by Hasbro, Activision, or the Transformers franchise owners.

## Current next step

Start with a written executable-identification report: file extension, file size, and locally observed file signature. Keep the actual game file private. No claims of successful recompilation or Android compatibility should be made until demonstrated by a reproducible test.

## Contributions

Please open an issue describing the research question, evidence, and proposed next step. Keep reports factual and avoid uploading copyrighted game data.
