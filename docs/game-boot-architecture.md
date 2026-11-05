# Game Boot Architecture and Milestone Gates

## Purpose

This document defines the implementation target for a native Android ARM64 static-recompilation port of Transformers: War for Cybertron (Xbox 360). It is an engineering plan, not evidence that any subsystem is already implemented or that the game currently boots.

The current Android app is only a diagnostic prototype. Its synthetic PowerPC interpreter is not the game runtime. The goal is to replace that path incrementally with a pinned ReXGlue-based title runtime while retaining small, independently testable diagnostics where useful.

## Required boot pipeline

A game boot attempt needs all of the following layers. The layers are deliberately separated so failures can be localized.

1. **Title input and code generation (host-side)**
   - Accept a user-selected, legally obtained XEX from a local, private working directory.
   - Inspect the XEX/PE structure and report unsupported or malformed inputs without uploading the file.
   - Run the pinned ReXGlue host CLI with a Transformers-specific manifest and function-boundary configuration.
   - Keep generated C++ and any derived game-specific outputs private; never commit them or publish them as CI artifacts.
   - Record tool versions, manifest version, input hash locally, and code-generation diagnostics without recording copyrighted content.

2. **Android ARM64 native build**
   - Build the pinned ReXGlue runtime for Android ARM64 with the reviewed Android compatibility patch set.
   - Compile title-generated C++ for arm64-v8a and link it against the runtime.
   - Package all required native shared libraries, including the Xenos GPU plugin, with the correct names and load paths.
   - Keep host tools separate from Android target libraries.

3. **Executable and guest-memory initialization**
   - Parse XEX security/optional headers and PE headers with bounds checks.
   - Handle the title's compression/encryption requirements only through supported, lawful mechanisms; fail closed when unsupported.
   - Map guest image sections, apply relocations, establish expected address ranges, and initialize zero-fill/BSS.
   - Load imports and resolve required Xbox kernel/runtime exports.
   - Set up guest stack, TLS, thread state, entry point, and initial CPU context.

4. **Xbox 360 runtime compatibility**
   - Implement or integrate the title-required kernel and system APIs: memory, files, events, synchronization, threads, timers, and other imports.
   - Match guest calling conventions, exception behavior, atomics, and memory-ordering assumptions.
   - Track unresolved imports and unsupported behavior explicitly; never silently stub a required API as successful.

5. **PowerPC translation and execution**
   - Use generated title code rather than the current synthetic interpreter for the real title path.
   - Validate integer, floating-point, branch, vector/VMX128, and special instruction behavior against focused tests.
   - Implement required guest memory access, endianness, reservation/atomic semantics, and helper functions.
   - Add title-specific fixes only with a reproducible test and a documented reason.

6. **Graphics, audio, input, and Android lifecycle**
   - Integrate the Xenos GPU translation/plugin path and map required guest resources, shaders, and render targets to Vulkan on Android.
   - Integrate audio, controller/touch input, window/surface lifecycle, pause/resume, and storage access.
   - Handle device loss, app backgrounding, and resource recreation.
   - Verify the actual Android GPU and Vulkan feature requirements before promising compatibility.

7. **Title boot and runtime validation**
   - Produce a private local build from the user's own generated code and game data.
   - Record whether the process starts, executable entrypoint is reached, kernel imports resolve, renderer initializes, and the title reaches a visible frame.
   - Distinguish a process launch, entrypoint reached, title screen, gameplay, and sustained playable performance. These are separate milestones.

## Source tree target

The following layout is the intended separation. Directories and files should be added only as their interfaces and implementation are understood; empty placeholder files do not count as progress.

- `thirdparty/rexglue-sdk/` — pinned upstream SDK checkout, created by a reproducible setup script; do not commit generated vendor checkout unless licensing and repository policy explicitly allow it.
- `patches/rexglue/` — reviewed, version-pinned Android patch series with provenance and checksums.
- `tools/host/` — host-side XEX inspection and ReXGlue code-generation orchestration.
- `config/transformers-wfc/` — title manifest, function boundaries, and project-owned configuration; no game binary or copyrighted assets.
- `generated/private/` — local-only generated C++ and metadata; ignored by Git and never uploaded by CI.
- `android/app/src/main/cpp/` — Android JNI/SDL entry point, ReXGlue runtime integration, GPU plugin packaging, and Android-specific platform glue.
- `android/app/src/main/java/` — Android activity, permissions, lifecycle and document-picker integration.
- `tests/fixtures/` — original or synthetic test inputs only.
- `docs/compatibility/` — import, instruction, graphics, audio and boot-progress matrices.

## APK build-and-inspect loop

The APK is a milestone verification artifact, not the primary deliverable. For every source milestone:

1. Run host unit tests and static checks.
2. Build the host-side SDK/codegen tools when relevant.
3. Build the Android ARM64 debug APK.
4. Inspect the APK package name, version name/code, supported ABI, native libraries, and expected plugin names.
5. Inspect generated APK contents and compare them with the milestone's required components.
6. Install and run on the target phone when an installation path is available; capture logs and crashes.
7. Record exactly what passed and failed. An APK that compiles is not proof of game boot.
8. Keep a versioned source milestone and a matching APK artifact. Do not call an APK "bootable" unless the game reaches the explicitly stated boot milestone.

CI may build game-independent SDK/runtime code and synthetic tests. CI must not receive the copyrighted XEX, assets, firmware, keys, generated title code, or a derived APK containing those materials. Title-specific builds remain local/private.

## Milestone sequence

### B0 — Architecture and provenance
- [x] Document the complete boot pipeline and verification gates.
- [ ] Audit licenses and exact pinned versions for ReXGlue, Android reference patches, SDL3, Vulkan dependencies, and any reused code.

### B1 — Reproducible SDK integration
- [ ] Add a pinned setup script that checks out ReXGlue v0.10.0 and verifies exact patch checksums.
- [ ] Apply only reviewed Android patches; record upstream commit/tag and patch provenance.
- [ ] Build the host CLI in CI without a game executable.
- [ ] Build the SDK's Android ARM64 runtime in CI without generated title code.
- [ ] Package and inspect a diagnostic APK containing the real SDK runtime and required native plugins.

### B2 — Title configuration and private code generation
- [ ] Determine the correct Transformers: War for Cybertron XEX entrypoint and manifest configuration from authorized local analysis.
- [ ] Run host code generation locally against the user's own XEX.
- [ ] Keep generated title code private and verify generated source inventory.
- [ ] Build a local APK from generated code and inspect it; do not upload the APK to public CI artifacts.

### B3 — Executable loading and kernel imports
- [ ] Implement/validate XEX/PE image loading, memory mapping, relocations, entrypoint and imports.
- [ ] Produce an import-resolution report; no unresolved required import may be silently ignored.
- [ ] Validate startup through controlled synthetic fixtures before attempting the game.

### B4 — CPU and runtime correctness
- [ ] Validate required PPC/VMX128 behavior and guest memory semantics.
- [ ] Implement title-required kernel APIs and threading/synchronization.
- [ ] Run focused tests and local title boot attempts; document every failure.

### B5 — Graphics, audio, and input
- [ ] Initialize the Android Vulkan/Xenos path and verify plugin loading.
- [ ] Implement title-required rendering, audio and input behavior.
- [ ] Validate visible frames and lifecycle behavior on the target phone.

### B6 — First boot and playable milestones
- [ ] Confirm the title reaches its entrypoint.
- [ ] Confirm a visible title screen.
- [ ] Confirm gameplay can be entered.
- [ ] Measure sustained frame rate, thermal behavior, memory use, and stability before making performance claims.

## Current known limitations

- No Transformers-generated code is present.
- No complete XEX loader or Xbox kernel compatibility layer is present.
- No ReXGlue runtime is currently linked into the app.
- No Xenos renderer, audio implementation, or title input integration is present.
- No game boot has been demonstrated.
- The current synthetic PPC self-test is not evidence of game compatibility.

Every checkbox above is evidence-gated. A code file existing is not enough to mark a subsystem complete.
