# Cybertron Recomp Labs

**Explore. Experiment. Recompile.**

Independent fan research into running the Windows PC release of Transformers: War for Cybertron on Android ARM64.

> Status: Windows compatibility foundation. No playable port or game boot has been achieved.

## Direction
The active target is now the Windows PC release. Xbox 360 / PowerPC code is being retired from this branch. The intended architecture combines an Android host, a Windows API compatibility layer such as Wine, a CPU translator suitable for the executable's actual architecture, and compatible graphics/audio/input backends. [Winlator](https://github.com/brunodev85/winlator) is the main Android architecture reference, not a drop-in Transformers runtime.

## Current app
The Android app is a basic shell for selecting the user's own Windows executable and game installation directory. It does not yet contain Wine, CPU translation, DirectX translation, or a game runtime, and cannot launch the game.

## Device target
Initial test target: Snapdragon 6 Gen 1 / Adreno / Android ARM64. The desired 18–22 FPS is an unverified development target, not a promise. Stability, thermals and frame pacing take priority.

## Rules
- Users provide their own legally obtained PC game files.
- Never commit or distribute the game's executable, assets, music, videos, or proprietary data.
- Do not use leaked source code, firmware, encryption keys, or proprietary game data.
- Check upstream licences and notices before reuse or redistribution.
- Unofficial fan research; not affiliated with Hasbro, Activision, Microsoft, or the franchise owners.

## Build
GitHub Actions builds and packages the Android app shell only. A green CI run proves compilation and packaging, not runtime integration, game boot, or gameplay.

See [the PC compatibility plan](docs/pc-compatibility-plan.md).
