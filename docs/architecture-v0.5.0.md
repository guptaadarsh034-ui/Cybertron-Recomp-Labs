# Android ARM64 research architecture — v0.5.0

## What this milestone contains

The Android application is a native ARM64 debug prototype. The Kotlin activity owns the Android UI and local document-picker flow. JNI is a narrow bridge to a platform-independent C++ PowerPC interpreter core.

## Native core layout

- `android/app/src/main/cpp/ppc_core.hpp`: CPU state and interpreter API.
- `android/app/src/main/cpp/ppc_core.cpp`: bounded instruction execution and guest-memory access.
- `android/app/src/main/cpp/ppc_runtime.cpp`: JNI bridge and synthetic self-test program.
- `tests/native/ppc_core_tests.cpp`: host-native regression tests compiled and run in GitHub Actions.

## Implemented PowerPC subset

This prototype currently implements only:
- `addi`, `addis`
- `ori`, `oris`
- `add` (X-form XO 266; integer result only)
- `lwz`, `stw` using big-endian guest memory
- `b` (unconditional branch, AA and LK handling)

The interpreter enforces 4-byte instruction alignment, checks guest memory bounds and word alignment, rejects unsupported instructions, validates branch targets within the supplied program, and limits the number of executed instructions to prevent unbounded guest loops.

## Explicitly not implemented

This is not a complete PowerPC ISA implementation. It does not implement VMX/VMX128, floating point, condition-register semantics, XER/CTR, most branch-conditional forms, exceptions, threads, cache behavior, MMIO, Xbox 360 kernel APIs, XEX parsing/decryption, static recompilation, Xenos graphics, game audio, or game boot.

The app's executable inspection remains a local preflight only: it reads file metadata and the first four bytes. It does not upload, parse, decrypt, or execute the selected file.

## Validation

The CI workflow compiles the core with the host C++ compiler using warnings-as-errors and runs seven deterministic regression cases. It then builds the Android arm64-v8a debug APK with the configured Android NDK and CMake toolchain.

A green CI build proves compilation and these synthetic tests only. It does not prove compatibility with an Xbox 360 executable or playable game performance.
