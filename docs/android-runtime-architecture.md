# Android ARM64 Runtime Architecture

## Target and current milestone

- Target platform: Android on ARM64 (AArch64).
- Android application ID: `com.cybertron.recomplabs`.
- Current diagnostic app version: 0.6.0.
- Current milestone: a bootable Android diagnostic shell that calls a small native PowerPC interpreter through JNI and performs limited, local XEX/PE structural checks.
- This is not yet a game runtime, static recompiler, Xbox 360 emulator, or playable port.

The exact SoC, Android version, ABI availability, and GPU capabilities must be measured on the target device rather than assumed from the marketing name.

## Current layered design

```text
Android launcher activity / diagnostic UI
  - app identity and build information
  - local document picker
  - synthetic CPU self-test action
          |
          +---- XEX structural preflight (Kotlin)
          |
          +---- JNI boundary
                    |
              Native ARM64 library
                - synthetic test program
                - PowerPC interpreter core
                    |
              Guest CPU state / limited instruction subset
```

## Current repository components

- `android/app/src/main/java/com/cybertron/recomplabs/MainActivity.kt`: Android launcher and diagnostic UI.
- `android/app/src/main/java/com/cybertron/recomplabs/XexInspector.kt`: bounded local structural preflight for selected files.
- `android/app/src/main/cpp/ppc_runtime.cpp`: JNI entry point and synthetic test harness.
- `android/app/src/main/cpp/ppc_core.cpp` and `ppc_core.hpp`: small PowerPC interpreter core.
- `tools/ppc_inspect.py`: separate educational word-inspection tool.
- `tests/`: regression tests using project-owned synthetic inputs.

## First APK acceptance criteria

1. Compile the native runtime for `arm64-v8a` with the Android NDK.
2. Assemble the Android debug APK with Gradle.
3. Verify the APK package name and version metadata.
4. Install the APK on a real ARM64 Android device.
5. Confirm the launcher activity opens without crashing.
6. Run the JNI synthetic PowerPC self-test and record its output.
7. Confirm that local XEX preflight reports only the limited checks actually implemented.

CI success proves the build and packaging checks only. Device installation, launch, and JNI interaction require a separate real-device test.

## Deliberate limitations

The current interpreter supports only a tiny subset of PowerPC instructions. It does not implement the complete CPU architecture, VMX/VMX128, full memory and exception behavior, Xbox kernel APIs, import resolution, guest threading, XEX security/decompression, Xenos graphics, or the Xbox audio stack. The current app does not execute game code.

A passing XEX structural check does not mean the file can boot. A successful Android APK build does not mean the game can run.

## Next engineering milestones

1. Verify the v0.6.0 APK builds and installs on the target device.
2. Record device ABI, Android API level, memory, and GPU capabilities.
3. Expand the synthetic PowerPC core with bounded guest memory, condition-register and link-register state, and reference-based tests.
4. Investigate XEX/PE loading with synthetic fixtures and public documentation.
5. Define the minimum Xbox runtime and graphics compatibility surface before attempting game execution.

## Legal boundary

Do not commit commercial game executables, copyrighted assets, firmware, encryption keys, or proprietary game data. Use synthetic programs and material that the project is authorized to inspect.
