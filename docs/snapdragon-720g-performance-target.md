# Snapdragon 720G minimum-device performance target

## Product target

Set Qualcomm Snapdragon 720G (SM7125-class, Adreno 618) as the **minimum compatibility and performance-validation target** for the Android runtime. This is a design target, not a claim that the current app or the full game already runs on it. The 720G is an ARM64 mobile SoC with a mid-range CPU/GPU; do not assume flagship-class throughput or any GPU feature without runtime detection.

The app must remain installable on supported 64-bit Android devices, and the runtime must select conservative paths on the 720G. A successful APK launch is a much lower bar than executing the Xbox 360 title at playable speed. First boot, stable menu, and sustained gameplay are separate goals and must be measured separately.

## Compatibility policy

- [ ] Keep arm64-v8a as the baseline native ABI; do not make 32-bit ARM or x86 a prerequisite.
- [ ] Establish the actual minimum Android API from dependencies; keep optional platform APIs guarded by SDK checks.
- [ ] Query device ABI, SoC model, Android version, memory class/low-RAM status, Vulkan device/API/extensions and driver at runtime.
- [ ] Never identify the GPU or Vulkan capability solely from a marketing device name; query Vulkan and validate required extensions/features.
- [ ] Provide a conservative fallback when Vulkan features are absent; do not require optional extensions for app launch.
- [ ] Do not hard-code Snapdragon-only instruction sets. Detect CPU features before selecting optimized ARM64 code paths.
- [ ] Keep a portable baseline implementation and dispatch to optimized kernels only after feature checks.
- [ ] Treat thermal throttling, background limits and memory pressure as first-class runtime conditions.

## CPU and recompiler strategy

The CPU is likely to be the principal bottleneck for a full Xbox 360 workload on this class of handset. The primary performance strategy should be to reduce guest instructions and host overhead, not simply raise compiler optimization flags.

- [ ] Profile representative synthetic guest workloads on actual 720G hardware before choosing an execution strategy.
- [ ] Prefer a validated ARM64 dynamic/static recompiler or hybrid interpreter/recompiler over interpreting every guest instruction indefinitely, if project scope and correctness allow.
- [ ] Compile generated ARM64 code for a conservative baseline; use runtime-dispatched optional optimizations only where measured benefit justifies complexity.
- [ ] Cache translated blocks with bounded memory use and correct invalidation.
- [ ] Use fast dispatch, predecoded instructions and hot-block compilation only after correctness tests.
- [ ] Avoid per-instruction heap allocation, string formatting, locks and JNI transitions.
- [ ] Keep guest execution in native code; cross the JNI boundary at coarse-grained calls, not for each guest instruction.
- [ ] Use bounded worker threads. Do not assume eight CPU cores means eight high-performance cores; avoid oversubscription.
- [ ] Benchmark single-thread latency as well as multi-thread throughput, frame pacing and thermal steady state.
- [ ] Preserve a deterministic interpreter/reference mode for debugging and differential testing.

## Memory strategy

- [ ] Replace the prototype's fixed 4 KiB guest memory with a sparse/region-based guest address space.
- [ ] Reserve/map memory lazily where safe; avoid allocating large host buffers before the game requires them.
- [ ] Reuse translation caches, command buffers and temporary decode structures.
- [ ] Avoid copying full XEX images between Kotlin and native code; stream or map bounded chunks and transfer ownership explicitly.
- [ ] Use explicit memory budgets for guest RAM, translated code, textures, render targets, audio and caches.
- [ ] Respond to Android memory trim callbacks by dropping rebuildable caches and nonessential resources.
- [ ] Detect allocation failures and report the subsystem and requested size rather than crashing or silently corrupting memory.
- [ ] Measure peak resident memory on device; 8 GB advertised device RAM is not the app's available memory budget.

## GPU strategy

Adreno 618 is the 720G target GPU, but actual Vulkan API level, extensions, driver quality and memory budget must be queried on each device.

- [ ] Build a Vulkan renderer with a clearly defined minimum feature set, validated on Adreno 618.
- [ ] Avoid requiring ray tracing, mesh shaders, descriptor indexing or other high-end optional features.
- [ ] Use shader/pipeline caches with bounded size and robust invalidation by driver/device identity.
- [ ] Minimize render-target count, resolution, bandwidth, overdraw and unnecessary format conversions.
- [ ] Add dynamic internal resolution and scalable graphics settings after correctness is established.
- [ ] Batch command submission and resource updates; avoid per-draw JNI or Kotlin overhead.
- [ ] Test tiled/swizzled Xbox texture conversion and shader translation independently of game boot.
- [ ] Profile GPU time and CPU submission time separately; do not infer GPU bottlenecks from low frame rate alone.

## Audio, input and scheduling

- [ ] Use a low-latency Android audio backend with bounded buffers and no allocation in the real-time callback.
- [ ] Keep audio mixing isolated from guest CPU and render-critical work.
- [ ] Poll/batch controller input at a stable cadence; avoid busy loops.
- [ ] Pace frames using Android display timing and avoid unrestricted render loops.
- [ ] Prioritize UI responsiveness and audio deadlines; make guest execution yield under thermal/memory pressure.
- [ ] Pause or reduce work when the app is backgrounded.

## Performance acceptance tests

Run tests on a physical Snapdragon 720G / Adreno 618 device, not only an emulator or CI runner. Record exact device model, OS build, driver versions, ambient conditions and test duration.

- [ ] Cold launch and warm launch time.
- [ ] Peak and steady-state resident memory.
- [ ] CPU time per fixed synthetic guest instruction workload.
- [ ] Recompiler throughput and translation-cache hit rate.
- [ ] Vulkan renderer CPU/GPU frame times at multiple internal resolutions.
- [ ] Audio underruns and controller/input latency.
- [ ] 20–30 minute sustained run with thermal state and performance drift.
- [ ] No crashes, ANRs, unbounded cache growth or thermal runaway.
- [ ] Repeat key tests on the user's OPPO handset as a second device target.

## Performance tiers

- **Tier A — 720G baseline:** correctness first, conservative resolution, bounded memory and stable frame pacing. This is the minimum validation device.
- **Tier B — newer mid-range / user's OPPO:** higher internal resolution and cache budgets only when measured headroom exists.
- **Tier C — flagship:** optional higher-quality settings; must not become a dependency of the baseline implementation.

Do not promise a fixed FPS or playable Transformers experience until the real title has passed device tests. If the 720G reaches only a boot logo or menu, report that honestly and optimize the measured bottleneck.

## Immediate implementation sequence

1. Add runtime hardware/driver diagnostics and a device capability report.
2. Establish baseline CPU, memory and GPU microbenchmarks on the 720G.
3. Remove avoidable interpreter/JNI overhead and create a portable native performance baseline.
4. Integrate and validate a recompiler path with differential correctness tests.
5. Implement guest memory and graphics with explicit budgets and scalable quality.
6. Profile on the physical 720G, then tune; do not guess based on SoC marketing specs.