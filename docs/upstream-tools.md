# Upstream Xbox 360 Recompilation Tools

This project should build on existing open-source work instead of pretending to implement a full Xbox 360 recompiler from scratch.

## Candidate upstream projects

### 1. Xenon-Recomp

Repository: https://github.com/nimauria/Xenon-Recomp

Xenon-Recomp is an experimental Xbox 360 ahead-of-time recompilation framework. Its pipeline analyses Xbox 360 PowerPC code and generates C++ intended to run with a shared runtime. The repository also describes tools for PowerPC disassembly, IR inspection, import scanning, and module inspection.

**Important platform limitation:** its current documented host targets are Windows and Linux on x86-64; Android ARM64 is not a supported target today. It is useful as a research reference and possible upstream to evaluate, not a ready-made Android port. Its own README says real-title compatibility is experimental and that recompilation success does not guarantee a game is playable.

### 2. 360tools / ReXGlue

Repository: https://github.com/sp00nznet/360tools

This toolkit documents a workflow built around ReXGlue for Xbox 360 XEX/STFS handling, PowerPC-to-C++ code generation, runtime integration, and troubleshooting. Its documented build requirements include a desktop C++ toolchain and Windows-oriented components, so it is not a phone-only or Android-ready solution either.

## Decision for Cybertron Recomp Labs

1. **Use Xenon-Recomp as the first architecture/code-analysis reference** because its stated purpose is Xbox 360 PowerPC recompilation and its repository documents standalone analysis tools.
2. **Study 360tools/ReXGlue as a second reference** for XEX intake, import/stub workflows, runtime integration, and test harness ideas.
3. Do not copy or vendor large upstream codebases until their licenses, dependencies, target platforms, and maintenance state have been reviewed.
4. Keep this repository focused on game-independent research notes, reproducible tests, and any original Android/ARM64 feasibility work.
5. Do not claim that either upstream project currently produces a working *Transformers: War for Cybertron* Android build. That has not been demonstrated here.

## Phone-only workflow

An Android phone can be used to read source, edit documentation, track issues, and make small changes through GitHub's website. It cannot by itself satisfy the desktop compiler/runtime requirements documented by these projects. A browser-based cloud development environment may help with some Linux builds if the account's available resources and the project's dependencies are sufficient, but Android-device output and GPU/runtime compatibility would still need separate validation.

## Immediate next research tasks

- [ ] Compare Xenon-Recomp's license, build dependencies, and host-platform assumptions with the Android ARM64 goal.
- [ ] Identify the smallest standalone analysis utility that can be exercised in a browser/cloud environment.
- [ ] Write down the expected input/output and create synthetic test fixtures before inspecting any game-specific executable.
- [ ] Record unsupported instructions, imports, and runtime gaps as evidence rather than guessing.
