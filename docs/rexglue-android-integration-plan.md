# ReXGlue Android integration plan

This document tracks the path from the current Android ARM64 prototype to a real, locally provisioned Xbox 360 recompilation runtime.

## Goal

Build an Android ARM64 APK that can boot one legally owned, user-supplied game. The app and its runtime dependencies should be packaged with the APK where licenses permit; game executables, extracted game assets, keys, and generated game-specific code remain local and must never be committed.

**Important:** this plan is not a claim that the current APK can boot a game. A file picker and a successful Android build alone do not constitute game support.

## Approach

Use ReXGlue as the candidate static-recompilation backend, adapting its Android integration to this repository rather than copying another game's generated code or game-specific patches. ReXGlue's host code-generation step is separate from the on-device ARM64 runtime; the target game needs a compatible manifest and generated sources before a runtime can start it.

Reference implementation for architecture and build-system research:
- https://github.com/deivid22srk/hells-gate-recomp-android
- https://github.com/rexglue/rexglue-sdk

These are references, not drop-in support for every Xbox 360 title. Compatibility depends on the selected game's executable, manifest, imported functions, graphics/audio paths, and runtime requirements.

## Milestones

### 1. Reproducible SDK/toolchain setup
- Pin the ReXGlue SDK revision and Android NDK/CMake/Clang versions.
- Add setup and host-codegen scripts that are safe to rerun and fail with actionable diagnostics.
- Keep downloaded SDK/build output and all game-specific inputs out of version control.
- Fix the existing host-toolchain build issue independently from Android runtime work.

### 2. One-game local code generation
- Select one legally owned title as the first target.
- Accept the user's local XEX and required extracted files; do not upload them to CI or GitHub.
- Add a title-specific manifest only after verifying it against the actual executable.
- Generate C++ sources locally and report missing imports, unsupported instructions, and codegen errors clearly.

### 3. Android runtime integration
- Integrate the matching ReXGlue runtime and required SDL/Vulkan/native dependencies for `arm64-v8a`.
- Wire the app's document picker to a persistent local game directory or URI strategy appropriate to the runtime.
- Add startup validation for missing files, permissions, incompatible manifests, and unsupported devices.
- Preserve the existing launcher as a diagnostic/fallback screen until the runtime is ready.

### 4. First real boot
- Launch generated game code from the Android app and initialize the game's required subsystems.
- Verify graphics, input, audio, storage, and clean shutdown on a physical ARM64 device.
- Distinguish “APK installs,” “runtime initializes,” “game reaches title screen,” and “playable gameplay” as separate test outcomes.

### 5. Packaging and release
- Package only redistributable runtime dependencies.
- Produce an APK artifact only after an explicitly approved build.
- Document local setup, device requirements, game-file placement, known limitations, and license notices.

## Acceptance checklist

- [ ] SDK setup is pinned and reproducible.
- [ ] Host code generation succeeds for the selected local game.
- [ ] Android ARM64 native libraries compile and link.
- [ ] App can locate and validate the selected game's local files.
- [ ] Runtime starts generated game code without crashing.
- [ ] Game reaches a verified title screen on a physical device.
- [ ] Input and graphics work; audio is checked separately.
- [ ] No copyrighted game files, generated title-specific sources, keys, or secrets are committed.
- [ ] APK build and artifact publication are explicitly approved before execution.

## Guardrails

- Work on the `feat/rexglue-android-integration` branch in the existing Cybertron-Recomp-Labs repository.
- Do not merge to the default branch.
- Do not trigger GitHub Actions or other builds without approval.
- Do not add proprietary game assets, executables, keys, or generated game-specific code to commits.
- Do not claim game compatibility until the acceptance checklist has been tested.
