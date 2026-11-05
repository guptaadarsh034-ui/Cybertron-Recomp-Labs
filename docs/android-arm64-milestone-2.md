# Android ARM64 Milestone 2

## Goal
Extend the synthetic PowerPC interpreter with a small, bounded guest-memory model, 32-bit big-endian loads/stores, and an unconditional branch. Keep the test program synthetic and self-contained.

## Added behavior
- `lwz` (opcode 32): aligned 32-bit load from guest memory.
- `stw` (opcode 36): aligned 32-bit store to guest memory.
- `b` (opcode 18): unconditional relative or absolute branch, including link-bit handling.
- A bounded fetch/execute loop with a maximum instruction count.
- Explicit errors for unsupported instructions, unaligned accesses, and out-of-range guest-memory accesses.

Guest memory is currently 256 bytes. This is a learning prototype, not a complete PowerPC implementation. It does not yet implement conditional branches, the condition register, exceptions, floating point, VMX/VMX128, caches, threads, or Xbox 360 APIs.

## Synthetic self-test
The app's built-in program:
1. Sets r3=7 and r4=5.
2. Adds them into r5 (12).
3. Stores r5 at guest address 16 and loads it into r6.
4. Branches over a deliberately unsupported instruction word.
5. Executes the branch target, setting r7=1.

Expected result:
- `Synthetic PowerPC test: PASS`
- `r5 = 12`
- `r6 (lwz) = 12`
- `r7 (branch target) = 1`
- 7 guest instructions executed; final guest PC is `0x20`.

## Validation
The Android GitHub Actions workflow builds the ARM64 debug APK on pushes affecting `android/**`. A successful build verifies compilation and packaging only. Install the artifact and run the self-test on a physical ARM64 Android device to validate runtime behavior.

## Next steps
1. Add dedicated native unit tests for memory boundaries, alignment, sign extension, and branch targets.
2. Add conditional branches and condition-register semantics.
3. Introduce a PowerPC intermediate representation before attempting ARM64 code generation.
4. Research executable loading and platform APIs independently, using only legally obtained test material.
