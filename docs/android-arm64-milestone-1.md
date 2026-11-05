# Android ARM64 Milestone 1

## Target device
OPPO F29 5G (8 GB RAM, 256 GB storage). The app targets Android's `arm64-v8a` ABI. Confirm the actual Android version and ABI on-device before making compatibility or performance assumptions.

## Goal
Run a synthetic PowerPC instruction sequence inside a native Android application and display the guest CPU state. This validates the app/JNI/native-build path and a tiny interpreter loop only.

## Prototype
- Kotlin activity provides a button and result display.
- CMake builds a C++ shared library for `arm64-v8a`.
- JNI invokes a small interpreter.
- Synthetic guest program sets r3=7, r4=5, then adds them into r5.
- Expected result: r5=12 after three guest instructions.

Supported instructions: `addi` (opcode 14), `ori` (opcode 24), and `add` (opcode 31, XO 266). Unsupported instructions fail explicitly. This is not a static recompiler, full PowerPC implementation, Xbox 360 emulator, or game loader. It does not implement VMX128, guest memory, branches, exceptions, or Xbox kernel APIs.

## Build requirements
- JDK 17
- Android SDK Platform 35
- Android NDK and CMake 3.22.1
- Gradle compatible with Android Gradle Plugin 8.7.3 (Gradle 8.9 recommended)

Open the `android/` directory as an Android Gradle project in Android Studio, or use a Codespace with Android SDK, NDK, CMake, and JDK configured. The current Python-only Codespaces devcontainer does not install the Android toolchain automatically.

## First validation
1. Build and install the debug APK on an ARM64 Android device.
2. Open Cybertron Recomp Labs and tap **RUN POWERPC SELF-TEST**.
3. Confirm output says `Synthetic PowerPC test: PASS` and `r5 = 12`.
4. Record Android version, ABI, build logs, and test result. Do not upload game files or copyrighted assets.

## Next steps
1. Add automated host-side instruction-semantics tests.
2. Add guest memory and a bounded instruction fetch/execute loop.
3. Add branch instructions and condition-register behavior.
4. Design a translator IR before generating ARM64 machine code.
5. Research XEX/PE loading and Xbox 360 system APIs independently.
