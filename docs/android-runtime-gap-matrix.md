# Xbox 360 → Android ARM64 runtime gap matrix

This is an engineering inventory for the first real boot of Transformers: War for Cybertron. It is deliberately exhaustive at the subsystem level: every item must be implemented, supplied by a compatible upstream component, or explicitly proven unnecessary for the chosen title build. A checked box means tested evidence exists, not merely that a class or stub exists.

## 0. Build, device and diagnostics

- [ ] Pin Android Gradle Plugin, Gradle, Kotlin, NDK, CMake and compiler versions.
- [ ] Build reproducibly for arm64-v8a; record APK checksum, native ELF dependencies and build provenance.
- [ ] Install/launch on the actual OPPO device; record Android release, ABI, SoC, Vulkan driver/device/extensions, memory class and thermal behaviour.
- [ ] Add structured log levels, persistent per-session logs, guest PC/register snapshots, crash tombstones and export of diagnostics.
- [ ] Add a clear capability/status screen distinguishing package build, app launch, XEX validation, guest execution, rendered boot and gameplay.
- [ ] Keep game images, title updates, firmware, keys and copyrighted assets out of source control, CI and issue attachments.

## 1. Game identity and input handling

- [ ] Identify exact game release: region, media/title ID, executable version and title update.
- [ ] Support Android Storage Access Framework document URIs without assuming filesystem paths or seek support.
- [ ] Handle unknown document sizes, short reads, non-seekable streams, cancellation and permission persistence.
- [ ] Validate container boundaries, integer overflow, alignment, offsets and count limits before allocation or seeking.
- [ ] Provide local-only diagnostics and hashes/metadata; never upload user game content.
- [ ] Create synthetic fixtures for valid, truncated, malformed and adversarial inputs.

## 2. XEX and PE loading

- [ ] Parse XEX2 header and optional-header directory according to the format, including endianness and field semantics.
- [ ] Parse security information and image metadata with strict bounds checks.
- [ ] Determine encryption/signing and compression format for the user's exact executable.
- [ ] Implement only legally supportable decryption/decompression paths; do not bundle or distribute console secrets or proprietary keys.
- [ ] Parse the embedded PE image and section table.
- [ ] Map sections at correct guest virtual addresses with permissions and alignment.
- [ ] Apply supported relocations and validate image base/entry point.
- [ ] Parse import libraries/thunks and build an explicit import manifest.
- [ ] Verify the mapped image against independent test fixtures and known metadata.
- [ ] Fail closed with actionable errors for unsupported protection, compression, relocation or image variants.

## 3. PowerPC/Xenon CPU

- [ ] Establish architectural state: GPRs, FPRs, LR, CTR, CR, XER, FPSCR, MSR and exception state.
- [ ] Implement complete instruction semantics required by the title, including branches, loads/stores, arithmetic, logical, rotate/mask, compare, floating point and system instructions.
- [ ] Implement VMX/VMX128 vector state and instruction semantics used by Xenon code.
- [ ] Correctly model endianness, alignment, reservation/atomic operations, exceptions and privileged behaviour needed by user code.
- [ ] Implement instruction decoding and precise unsupported-instruction traps.
- [ ] Build differential tests against a trusted reference or independently validated test vectors.
- [ ] Add deterministic stepping, instruction budgets, trace buffers and reproducible crash snapshots.
- [ ] Decide between interpreter, dynamic recompiler, static recompiler or a hybrid based on evidence and licensing.
- [ ] If recompiling, validate branch/link semantics, relocations, ABI, register allocation, stack frames and generated ARM64 correctness.

## 4. Guest memory and address space

- [ ] Replace the current 4 KiB demo memory with a guest address-space abstraction.
- [ ] Support page mapping, permissions, alignment, big-endian guest accesses and unmapped-access faults.
- [ ] Implement image, heap, stack, shared and executable regions as required.
- [ ] Implement allocation/free and memory protection APIs expected by imported functions.
- [ ] Model memory-mapped I/O ranges separately from ordinary RAM.
- [ ] Add guard regions, allocation limits and overflow-safe address translation.
- [ ] Test cross-page accesses, invalid pointers, guest/host pointer separation and concurrency.

## 5. Xbox kernel and system libraries

- [ ] Enumerate imports from the exact XEX and group them by library/version.
- [ ] Implement an import resolver with exact ordinals/names and calling conventions.
- [ ] Implement required thread creation, scheduling, priorities, suspend/resume and termination.
- [ ] Implement events, semaphores, mutexes, wait operations and timers.
- [ ] Implement virtual memory, heaps, TLS and process/thread environment data.
- [ ] Implement file/device I/O abstractions used by the game.
- [ ] Implement required module/library loading and initialization order.
- [ ] Implement exception/structured error behaviour sufficiently for the title.
- [ ] Stub nothing silently: every placeholder must be logged, classified and tested for safety.
- [ ] Add API conformance tests for every imported function used before the first boot checkpoint.

## 6. Graphics and display

- [ ] Identify the title's graphics initialization path and required Xenos GPU features.
- [ ] Design a guest GPU command processor and resource/state model.
- [ ] Implement guest memory ↔ GPU resource mapping and synchronization.
- [ ] Translate shaders to a supported Android Vulkan path, including Xbox shader formats and semantics.
- [ ] Implement render targets, depth/stencil, textures, samplers, vertex/index formats and relevant tiling/swizzling.
- [ ] Implement required blend, raster, viewport, scissor and synchronization behaviour.
- [ ] Handle surface lifecycle, rotation, resolution scaling and Android context loss.
- [ ] Validate with synthetic GPU tests before expecting a game logo/menu.

## 7. Audio

- [ ] Identify audio API imports and formats used by the title.
- [ ] Implement guest audio buffers, voices, mixing, sample formats and timing.
- [ ] Connect output to Android low-latency audio (AAudio/Oboe or a justified alternative).
- [ ] Handle pause/resume, underruns, device changes and sample-rate conversion.
- [ ] Test deterministic audio buffer generation independently from the game.

## 8. Input and user experience

- [ ] Map Android touch controls to Xbox controller semantics.
- [ ] Support physical Bluetooth/USB gamepads, analog axes, triggers, dead zones and button remapping.
- [ ] Handle focus, controller disconnect/reconnect and Android lifecycle.
- [ ] Provide save-state/log export only after state consistency is established; do not advertise unsupported save states.
- [ ] Provide user-facing warnings for unsupported executable variants and missing runtime services.

## 9. Timing, threading and system integration

- [ ] Model guest timers, performance counters and clock sources.
- [ ] Implement deterministic scheduling primitives and synchronization semantics.
- [ ] Avoid blocking the Android UI thread; isolate guest execution and graphics/audio workers.
- [ ] Handle app pause/resume, background restrictions, process death and resource teardown.
- [ ] Profile CPU/GPU/memory use and thermal throttling on the actual phone.
- [ ] Establish safe frame pacing and configurable performance limits.

## 10. Recompiler/runtime integration

- [ ] Audit candidate upstream projects (including ReXGlue/XenonRecomp) for license, maturity, supported input, codegen targets and required runtime APIs.
- [ ] Pin upstream revisions and document local patches.
- [ ] Prove a synthetic guest function can be translated or interpreted, built for arm64-v8a, loaded and invoked through JNI.
- [ ] Prove generated code handles ABI, relocations, exceptions and imports before compiling game code.
- [ ] Keep host-only tools out of the Android app; define a reproducible offline pipeline for generated artifacts.
- [ ] Compare output from the recompiler with interpreter/reference results on test vectors.

## 11. Security, legal and reliability

- [ ] Keep all game content and proprietary console materials local to the user's device.
- [ ] Do not distribute copyrighted game assets, firmware, keys or title executable data.
- [ ] Document supported lawful input workflows and upstream licenses.
- [ ] Treat XEX files as untrusted input; fuzz parsers and cap memory/CPU/time.
- [ ] Avoid executing arbitrary host code from untrusted guest data.
- [ ] Add regression tests for crashes, malformed files and resource exhaustion.

## 12. Boot checkpoints and release gates

- [ ] G0: CI produces a reproducible arm64-v8a APK.
- [ ] G1: APK installs and launches on the OPPO device; logs captured.
- [ ] G2: XEX metadata and image layout validated for the exact title build.
- [ ] G3: XEX sections are loaded into guest memory and entry point validated.
- [ ] G4: Guest entry point executes with traceable CPU state and resolved imports.
- [ ] G5: Graphics initialization completes and a stable title splash/logo is visible.
- [ ] G6: Main menu is visible and input works.
- [ ] G7: A gameplay scene runs with graphics, audio and controller input.
- [ ] G8: Sustained play is stable enough to report performance and known limitations.

## Device-specific note

The user reports an OPPO F29 5G, 8 GB RAM and Snapdragon 6 Gen 1. Treat those as user-provided starting details, not verified hardware telemetry. Before performance claims, collect the exact Android build, Vulkan device/driver/extensions and thermal/memory data from the handset. Do not promise that the full game will run at an acceptable speed on this hardware.

## Current known gap

The existing app's PowerPC interpreter supports only a tiny demonstration subset (addi, addis, ori, oris, add, lwz, stw and unconditional branch) with 4 KiB of guest memory. The XEX inspector is a read-only structural preflight; it does not decrypt/decompress, map PE sections, apply relocations or execute the image. The app has no Xbox kernel, GPU, audio or controller runtime. Thus the current state is a research prototype, not an emulator or game port.
