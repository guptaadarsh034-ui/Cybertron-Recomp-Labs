# First boot-preflight APK — v0.6.0

## Purpose

This APK is the first Android ARM64 diagnostic build intended to inspect a user-selected Xbox 360 XEX file and report whether its basic container structure passes a limited preflight. It does not claim to boot or emulate the game.

## App flow

1. The user chooses a local document through Android's Storage Access Framework.
2. The app reads the XEX2 header, declared optional-header directory, PE data offset and four-byte PE signature.
3. It reports structural failures, or reports which limited checks passed.
4. The app explicitly states that the current build cannot boot the game.

The selected file is read locally. No game data is uploaded, copied into the repository, decrypted, or executed.

## Boot blockers

Even with a valid XEX2 header, a real boot attempt still requires XEX security/decompression support, PE image mapping and relocations, Xbox import and kernel compatibility, a much broader PowerPC implementation including VMX128, guest memory/MMIO and threading, and graphics/audio services compatible with the Xbox 360's Xenos GPU and audio stack.

## Interpretation

A passing preflight is not a compatibility verdict and does not prove the game can run. It only confirms the small set of header/container checks implemented in this build. A failed check can indicate an unsupported layout, malformed file, truncation, or a wrong file.

## Build validation

The GitHub Actions workflow must pass both the host-native PowerPC regression tests and Android arm64-v8a APK assembly. Only the APK artifact from a successful run of the v0.6.0 source should be installed for this milestone.
