# Upstream Xbox 360 Recompilation Tools

We should reuse mature, license-compatible upstream work rather than trying to write a complete Xbox 360 recompiler from scratch. This document separates **tooling that generates native code** from **runtime components needed to boot a particular game**.

## 1. ReXGlue SDK — primary candidate to evaluate

Repository: https://github.com/rexglue/rexglue-sdk  
Documentation: https://github.com/rexglue/rexglue-sdk/wiki  
License: inspect the upstream `LICENSE` and the exact pinned release before vendoring or modifying it.

ReXGlue is an experimental Xbox 360 static-recompilation SDK. It converts PowerPC code from XEX executables into C++ ahead of time and provides runtime facilities derived in part from Xenia. Its upstream documentation explicitly warns that it is **not a turnkey game launcher**: projects may need function-boundary configuration, missing kernel imports, and iteration on generated code. Graphics, audio, and input are still evolving.

**Important distinction:** building the ReXGlue command-line code generator is not the same as producing a playable game. Each title still needs its own generated code, runtime configuration, game data, and compatibility work.

## 2. Android ARM64 reference implementation

Repository: https://github.com/deivid22srk/hells-gate-recomp-android

This community project demonstrates an Android ARM64 integration of ReXGlue with the Android NDK/Gradle, SDL3, and Vulkan. Its source is useful for studying the native build wiring, Android lifecycle/context integration, file access, runtime library packaging, and GPU plugin loading.

Relevant implementation files to study:

- `scripts/setup-android.sh` — pins ReXGlue v0.10.0 and applies Android-specific patches.
- `scripts/host-codegen.sh` — builds the host-side CLI and runs code generation against a local `game/default.xex`.
- `android/app/build.gradle.kts` — Android SDK/NDK and ARM64 build configuration.
- `android/app/src/main/cpp/CMakeLists.txt` — builds generated game code and links the ReXGlue runtime.
- `patches/sdk/` — documents the Android compatibility changes made against the pinned SDK version.

This is a **reference implementation, not a drop-in Transformers port**. Its patches and build assumptions must be checked against the exact SDK version, and its game-specific generated code cannot be reused as Transformers code. Do not copy files blindly; review licenses for the app, SDK, dependencies, and patches first.

The example's build flow requires the title's XEX at code-generation time as well as appropriate game data at runtime. That has a practical consequence for our phone/browser-only workflow: a generic CI job can compile our existing shell, but it cannot generate Transformers-specific native code without access to an authorized local input. Do not commit a game executable or send it to a public build service.

## 3. Xenon-Recomp

Repository: https://github.com/nimauria/Xenon-Recomp

Xenon-Recomp is another Xbox 360 ahead-of-time recompiler and a useful reference for PowerPC analysis and code generation. Check its current README, license, target platforms, dependencies, and maintenance status before deciding whether any component is suitable for Android ARM64. Do not assume that code generation alone supplies the complete runtime required by a game.

## 4. Xenia

Repository: https://github.com/xenia-project/xenia

Xenia is an Xbox 360 emulator rather than a static recompiler. It is a reference for Xbox kernel behavior, guest memory, threading, graphics, audio, and title compatibility. Reuse code only after checking the relevant license, dependencies, and whether the component is architecturally compatible with a static-recompilation runtime.

## Decision for Cybertron Recomp Labs

1. **Study the Android ReXGlue reference first** to learn the real Android build/runtime architecture.
2. **Evaluate ReXGlue's exact release and license** before integrating it; the Android reference pins v0.10.0, so do not silently mix SDK versions.
3. **Keep our current app as a diagnostic prototype until the integration plan is validated.** It currently has only a small synthetic PowerPC interpreter and executable preflight checks; it is not yet linked to ReXGlue.
4. **Do not transplant another game's generated code or assets.** We need code generated from our own authorized Transformers executable and our own game-specific compatibility work.
5. **Use a staged proof:** first build the upstream SDK/toolchain in a cloud environment without game files; next validate an original/synthetic test; only then decide how game-specific code generation can be performed safely.
6. **Do not claim a game boot, playable APK, or performance result without a reproducible build and runtime evidence on the target device.**

## Phone-only workflow and constraints

A phone can be used to read source, edit files, and trigger supported cloud builds through GitHub. It does not itself provide a desktop host compiler, and the ReXGlue code-generation step is a host tool that processes the game's XEX. A cloud development environment may supply the host toolchain, but resource limits, Android SDK setup, dependencies, and access to game-specific input remain separate constraints.

## Next technical tasks

- [ ] Review the exact ReXGlue release license and its third-party dependency licenses.
- [ ] Map the Android reference's build steps to a **game-independent** CI smoke test.
- [ ] Identify which SDK patches are necessary for Android ARM64 and why.
- [ ] Compare the existing app's Gradle/CMake setup with the reference before changing architecture.
- [ ] Keep synthetic fixtures and test logs in the repository; keep game executables, assets, firmware, and keys out of Git.
- [ ] Only call the first milestone complete when the SDK builds reproducibly in CI. This would prove toolchain integration—not Transformers bootability.
