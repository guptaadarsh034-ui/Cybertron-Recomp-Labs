# First Android game-boot milestone

## Target

Reach the earliest reproducible execution milestone for a lawfully obtained copy of **Transformers: War for Cybertron (Xbox 360)** on the user's ARM64 Android device. A successful APK build or XEX header check is not a game boot.

## Current baseline

The Android app is a diagnostic prototype. It has a limited synthetic PowerPC interpreter test and a read-only XEX structural preflight. It does not load a game image into a guest address space, decrypt/decompress XEX sections, recompile a title, provide Xbox kernel imports, emulate Xenos graphics, or launch a game.

The current GitHub Actions workflow builds this Android prototype and separately builds the ReXGlue host CLI. Neither job by itself produces a playable game port. The host CLI currently runs on the CI host; it is not an Android ARM64 runtime.

## Milestones and exit criteria

### B0 — Reproducible Android package

- Build the existing debug APK for `arm64-v8a`.
- Verify package ID, version metadata, native library ABI and artifact checksum.
- Install and launch it on the target phone; record Android version and crash/logcat output.
- Do not describe this as game boot.

### B1 — Identify the user's game build

- Ask the user to identify the disc region/title ID and provide only locally generated metadata/logs.
- Keep ISO/XEX files, title updates, keys and copyrighted game data out of Git and CI artifacts.
- Validate file length and documented XEX fields with synthetic fixtures and bounds-checked tests.
- Record which XEX revision and required title update the work targets.

### B2 — Choose and validate the recompiler path

- Evaluate the pinned ReXGlue SDK and other public Xbox 360 recompilation/runtime projects for licensing, supported input format, generated-code target and runtime completeness.
- Do not assume a host x86-64 tool can emit a working Android ARM64 title module.
- Prove one synthetic PowerPC function can be translated, compiled for Android ARM64, loaded by the app and called through JNI before attempting a full title.

### B3 — Load and execute a minimal real guest entry point

- Implement a tested XEX loader pipeline: header validation, security/decompression handling where supported, PE image mapping, relocations, entry-point validation and import discovery.
- Add bounded guest memory, CPU state, diagnostics and deterministic tests.
- Resolve imports explicitly; fail with a useful unresolved-import report rather than pretending unsupported calls succeeded.
- The first boot checkpoint is entry-point execution with a trace and a known stop condition, not a rendered game.

### B4 — Reach an observable game checkpoint

- Implement the minimum generic Xbox kernel/runtime services demanded by the target executable.
- Add graphics initialization and a documented rendering backend; then pursue the earliest reproducible splash/logo/menu checkpoint.
- Capture logs, guest PC, unresolved imports, thread/event state and GPU command progress on every attempt.
- Keep title-specific discoveries in a separate profile/module, not hard-coded into generic CPU or kernel code.

### B5 — Device validation

- Test on the actual phone and record chipset, Android version, available RAM, Vulkan capabilities and thermal/performance behaviour.
- Treat Android ARM64 as a separate target requiring a compatible native module and runtime; a desktop build or successful CI packaging is not evidence of phone compatibility.

## Immediate next engineering task

Before attempting a full game boot, add host-side tests for malformed/truncated XEX headers and prove an end-to-end synthetic guest function can be compiled for `arm64-v8a`, loaded and called from the Android app. In parallel, inspect the existing ReXGlue host toolchain build logs and establish exactly what artifacts it produces. Only then decide whether to extend ReXGlue integration or adopt a more complete public runtime.

## Definition of success

Report each checkpoint separately:

1. APK compiled and packaged.
2. APK installed and app launched on device.
3. Target XEX accepted and loaded.
4. Guest entry point executed.
5. Game splash/logo rendered.
6. Main menu reached.
7. Gameplay observed.

Never infer a later checkpoint from an earlier one.
