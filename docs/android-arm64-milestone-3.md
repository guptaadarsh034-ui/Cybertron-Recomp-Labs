# Android ARM64 Milestone 3 — executable preflight

## Goal
Add a safe, local-only first step toward executable boot research: let the user select a file through Android's document picker and inspect its display name, reported size, and first four bytes.

## Current app behavior
- Retains the synthetic PowerPC interpreter self-test.
- Adds an Android document-picker action for a user-selected local file.
- Reads only the first four bytes and basic document metadata.
- Recognizes the literal XEX2 signature as a preliminary identification result.
- Does not upload, copy, decrypt, unpack, parse, recompile, or execute the selected file.

## What this milestone does not prove
An XEX2 signature is not proof that the file is a valid, complete, supported, or bootable Xbox 360 game executable. This is not an XEX loader, Xbox 360 emulator, static recompiler, or game boot attempt. No game content is included in the repository or APK.

## Next technical milestones
1. Add tests for malformed, truncated, and unknown headers using synthetic fixtures.
2. Research and document public XEX2 container metadata fields and validation rules.
3. Build a read-only parser for documented header structures, with bounds checks and no decryption or execution.
4. Design a PowerPC intermediate representation and instruction validation tests.
5. Only after executable parsing and a real translation/runtime path exist, plan a constrained boot experiment.

## Version
Android app version: 0.3.0 (versionCode 3). CI packages the APK and a ZIP with the version in both filenames.
