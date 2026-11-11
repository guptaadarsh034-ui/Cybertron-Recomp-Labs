# OPPO F29 5G — Android 16 device validation target

## Target profile

| Item | Target |
|---|---|
| Device | OPPO F29 5G |
| SoC | Qualcomm Snapdragon 6 Gen 1 |
| Memory | 8 GB RAM |
| Android | 16 (API level 36, as reported by the project owner) |
| Native ABI | `arm64-v8a` |
| APK | Debug build, currently version 0.6.0 |

The project owner supplied the device and OS details. Confirm the runtime ABI on-device before treating the profile as validated; the app displays Android's reported supported ABIs.

## Android 16 / native-library preparation

- Keep the native Android build restricted to `arm64-v8a` for this device target.
- Pin the Android NDK to `27.2.12479018` so local and CI native builds use the same toolchain.
- Enable `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES` for 16 KiB page-size-compatible native output.
- The CI workflow checks APK ZIP alignment with `zipalign -P 16` and checks the native ELF LOAD segment alignment.
- The app compiles against Android SDK 36 and targets API 36. The CI toolchain uses Android Gradle Plugin 8.10.1 and Gradle 8.11.1, alongside the pinned NDK.

## First phone test checklist

1. Install the CI-produced debug APK only after the workflow succeeds.
2. Confirm Android reports the package as installed and launches the app without crashing.
3. Confirm the app reports `arm64-v8a` among supported ABIs.
4. Run **SYNTHETIC POWERPC TEST** and capture the complete PASS report.
5. Select a harmless test fixture or a legally obtained local XEX file for the read-only preflight; do not upload game files.
6. Repeat launch and CPU-test checks after rotating the screen, backgrounding/resuming, and rebooting the phone.
7. Record app version, Android build number, ABI output, test result, and any crash logs.

## Acceptance criteria

- APK builds and passes the ZIP/ELF 16 KiB alignment checks.
- The APK installs and launches on the OPPO F29 5G running Android 16.
- The JNI synthetic PowerPC self-test reports PASS on the phone.
- The local XEX preflight handles malformed and valid-structure test inputs without crashing.

Passing these checks validates only the Android diagnostic shell and its small synthetic CPU test. It does not mean Transformers: War for Cybertron boots or is playable. Actual game execution remains blocked by XEX security/decompression, PE loading and relocations, Xbox kernel/import compatibility, broader PowerPC/VMX128 coverage, guest memory/MMIO/threading, and graphics/audio implementation.

## Device information still to capture

- Exact Android 16 build number / ColorOS version
- ABI list reported by the installed app
- Free storage before installation
- APK install result and launcher screenshot
- Synthetic CPU test output
- Crash logs, if any
