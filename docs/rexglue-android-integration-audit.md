# ReXGlue Android integration audit

**Status:** B1 investigation in progress. This document records source inspection only; it does not claim that the SDK has been integrated or built for Android.

## Pinned SDK

- Project: [ReXGlue SDK](https://github.com/rexglue/rexglue-sdk)
- Version: `v0.10.0`
- Verified release commit: `f5337cdc947ff6d4c4196737e2c807a48f2a1fc2`
- License at that tag: BSD 3-Clause (see the SDK's `LICENSE` file).
- Inspected upstream requirements: CMake 3.25 or newer, Clang 18 or newer, and C++23.
- The inspected upstream root CMake configuration recognizes Windows, Linux, and macOS. It does not provide an Android platform branch in the unmodified configuration.

Redistribution of SDK source or binaries must retain the BSD 3-Clause copyright notice, conditions, and disclaimer. The project must not imply endorsement by the SDK author.

## Android reference inspected

Reference implementation: [deivid22srk/hells-gate-recomp-android](https://github.com/deivid22srk/hells-gate-recomp-android).

Its README describes an Android arm64-v8a integration using Android NDK 27.2.12479018, JDK 17+, Clang 18+, CMake 3.25+, SDL3, and Vulkan. It applies three SDK patches against ReXGlue v0.10.0 and copies SDL3 Android Java glue.

This is a reference implementation for study, not a drop-in Transformers port. Its code generation manifest, game paths, generated C++, runtime assumptions, and game assets are specific to Dante's Inferno and must not be copied into this project as if they supported Transformers.

## Patch provenance gate

The reference repository exposes these patch paths:

- `patches/sdk/rexglue-sdk-v0.10.0.patch`
- `patches/sdk/rexglue-sdk-v0.10.0-android.patch`
- `patches/sdk/rexglue-sdk-v0.10.0-android-perf.patch`

The reference repository's root `LICENSE` path was not found during this audit. The SDK's BSD license does not automatically establish the license of independently authored changes in another repository. Therefore, do not vendor or redistribute those patch files until their authorship and applicable license/permission are confirmed. Their existence and described purpose are not evidence that they apply cleanly to our checkout.

## Safe repository boundaries

- `thirdparty/rexglue-sdk/` is a local pinned SDK checkout and is ignored by Git.
- `game/`, `game-files/`, and `local-inputs/` are private local input locations and are ignored.
- `generated/private/` is reserved for title-specific generated code and is ignored.
- Never put a game executable, game assets, firmware, encryption keys, or title-generated code in public commits or public CI artifacts.

## Next implementation gates

1. Confirm patch licensing/provenance or implement an independently authored Android adaptation.
2. Add a setup script that pins the SDK tag, verifies the exact checkout, initializes required submodules, and fails safely if the checkout is dirty or at the wrong revision.
3. Configure the SDK's host CLI separately from the Android target build.
4. Add Android platform support only behind a reproducible patch or maintained fork; do not pretend upstream v0.10.0 supports Android unmodified.
5. Build and inspect a diagnostic ARM64 APK without game-specific generated code.
6. Only then proceed to private, local Transformers-specific code generation using the user's own legally obtained game files.

## Evidence boundary

At the time of writing, this audit has not run the SDK setup, applied patches, compiled the SDK, built an APK, or launched a game. Those are future test results, not completed work.
