# Android ARM64 Runtime Architecture

## Target and scope

- Target device: OPPO F29 5G, 8 GB RAM, 256 GB storage.
- Target platform: Android on ARM64 (AArch64).
- First milestone: execute a tiny synthetic PowerPC program in a project-owned interpreter and verify its register results.
- Current status: initial host-buildable interpreter prototype; not an Android app, XEX loader, or game boot.

The exact SoC, Android version, ABI availability, and GPU capabilities must be measured on the target device rather than assumed from the marketing name.

## Layered design

```text
Android UI / diagnostics (future)
          |
JNI boundary (future)
          |
Native ARM64 runtime (Android NDK, future)
          |
PowerPC execution engine
  - instruction decode
  - guest CPU state
  - control flow
  - memory subsystem (not implemented)
          |
Xbox 360 compatibility services (future)
  - guest kernel APIs
  - file / threading / synchronization
  - graphics and audio interfaces
          |
Android platform services (future)
```

## Repository components

- `runtime/ppc/`: minimal C++ interpreter and synthetic smoke test.
- `tools/ppc_inspect.py`: separate educational word-inspection tool.
- `tests/`: Python tests for the inspector.
- `.devcontainer/`: browser-based development environment.

## First milestone acceptance criteria

1. Compile the C++ interpreter with a standard C++17 compiler.
2. Execute a synthetic three-instruction PowerPC program.
3. Verify that registers r3, r4, and r5 contain 40, 2, and 42.
4. Report unsupported instructions rather than silently guessing their behavior.
5. Run the same core in an Android ARM64 app in a later NDK integration milestone.

## Deliberate limitations

This prototype supports only a tiny instruction subset: `addi`, `addis`, `ori`, `add`, and a basic unconditional branch. It has no guest memory, exception model, floating-point unit, VMX/VMX128, system calls, thread scheduler, XEX parsing, import resolution, or graphics/audio implementation. It is not suitable for loading commercial game code.

The first branch implementation is intentionally minimal and does not yet model link-register semantics. The interpreter must gain a complete, tested CPU state and memory model before it can be used as a foundation for a real static recompiler.

## Next milestones

- Add a bounded guest-memory abstraction and load/store instructions.
- Add condition register and link register state, then correct branch semantics.
- Expand instruction coverage with reference-based tests.
- Add a small Android NDK app that invokes the same native core through JNI.
- Record device ABI, Android API level, available memory, and graphics capabilities from the OPPO device.
- Investigate executable formats and runtime imports using synthetic fixtures and public documentation only.

## Legal boundary

Do not commit commercial game executables, copyrighted assets, firmware, encryption keys, or proprietary game data. Use synthetic programs and material that the project is authorized to inspect.
