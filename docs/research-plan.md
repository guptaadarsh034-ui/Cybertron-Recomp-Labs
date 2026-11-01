# Research Plan

## Purpose

Determine, through small reproducible experiments, what would be required to translate an Xbox 360 game's PowerPC code into native ARM64 code and integrate the result with Android. This is feasibility research, not a claim that the game currently runs.

## Phase 1: Identify the input safely

Record only metadata from a locally held, legally obtained executable:

- Exact filename and extension
- File size in bytes
- File signature / identified container type
- Tool and version used to inspect it
- Any uncertainty in the identification

Do not upload the executable or proprietary game assets to this repository or a public analysis service.

## Phase 2: Establish references

Collect public technical references for:

- XEX container and PE image structure
- Xbox 360 PowerPC instruction set and calling conventions
- ARM64/AArch64 instruction set
- Static recompilation design patterns
- Android Native Development Kit (NDK), graphics APIs, audio, and input

Record source links, what each source supports, and unresolved questions. Do not copy proprietary code or bypass access controls.

## Phase 3: Build a tiny owned test case

Create synthetic instruction/data samples or a small original program. Define expected behavior and tests before writing a translator. Start with decoding and documenting a narrow instruction subset; do not imply that a toy test proves compatibility with the game.

## Phase 4: Evaluate dependencies

Inventory the operating-system calls, graphics behavior, audio, input, threading, timing, and file access that a full game runtime would need. Treat each dependency as an independent research question.

## Phase 5: Report evidence

For every experiment, record:

1. Question
2. Environment and tool versions
3. Input description (never copyrighted game data)
4. Steps to reproduce
5. Expected and observed results
6. Limitations and next experiment

## Success criteria

A milestone is complete only when its evidence is reproducible. A successful small instruction test is not evidence of a complete game port. A native Android build and game compatibility remain unproven until demonstrated on a device with authorized files.
