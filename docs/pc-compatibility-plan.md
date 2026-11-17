# Windows PC compatibility plan

## Decision
The active target is the Windows PC release of Transformers: War for Cybertron on Android ARM64. Xbox 360 / PowerPC code and documentation are removed from this branch.

## Architecture to validate
- Android host: lifecycle, document-provider access, storage, audio and input.
- Windows compatibility: Wine or a suitable derivative. Wine implements Windows APIs; it does not itself translate x86 CPU instructions.
- CPU translation: establish the executable's PE machine type and bitness first. Select a compatible translator only after this check; do not assume Box64 alone handles a 32-bit x86 executable.
- Graphics: identify the game's DirectX version and evaluate a Vulkan translation path such as DXVK where compatible.
- Runtime dependencies: inspect the local installation for redistributables, codecs, physics middleware and other dependencies.
- Game integration: use the user's private executable and installation directory. Never upload or bundle game files.

## Milestones
1. M0 — Retire Xbox/PowerPC code and update the Android shell.
2. M1 — Import the user's own executable and installation directory.
3. M2 — Audit PE architecture, imports and dependencies locally.
4. M3 — Select a licensed Android-compatible Wine and CPU-translation architecture.
5. M4 — Validate graphics, audio, input and storage backends independently.
6. M5 — Launch a harmless Windows test program.
7. M6 — Test the game's startup sequence, intro and main menu.
8. M7 — Test gameplay, stability, thermals and frame pacing.

A milestone is complete only with reproducible logs and evidence.

## Upstream reference
[Winlator](https://github.com/brunodev85/winlator) is the primary Android architecture reference. Study the exact source revision, dependencies and licence before reuse. Other upstreams to evaluate include [Wine](https://github.com/wine-mirror/wine), [Box86](https://github.com/ptitSeb/box86), [Box64](https://github.com/ptitSeb/box64), and [DXVK](https://github.com/doitsujin/dxvk).

## Current blockers
- No Windows compatibility runtime is integrated.
- No x86 translator is installed or built by this change.
- No DirectX/Vulkan translation layer is integrated.
- The game executable has not been inspected; its architecture and dependencies remain unknown.
- The previous cloud terminal lacked Android SDK/NDK and GitHub network access. This change does not install tools on the user's phone.
- Snapdragon 6 Gen 1 and the 18–22 FPS goal remain unverified.

## Build meaning
The Android CI workflow packages the app shell only. Green CI means APK compilation and packaging, not Windows runtime integration, game boot, or gameplay.
