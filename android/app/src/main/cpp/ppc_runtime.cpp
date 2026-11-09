#include <jni.h>

#include "ppc_core.hpp"

#include <cstdint>
#include <sstream>
#include <string>
#include <vector>

namespace {
std::string run_synthetic_self_test() {
    // Hand-encoded synthetic PowerPC instructions; this is not game code.
    const std::vector<std::uint32_t> program = {
        0x38600007U, // addi r3,r0,7
        0x38800005U, // addi r4,r0,5
        0x7ca32214U, // add r5,r3,r4
        0x90a00010U, // stw r5,16(r0)
        0x80c00010U, // lwz r6,16(r0)
        0x48000008U, // b +8, skip next word
        0x00000000U, // unsupported word; must not execute
        0x38e00001U  // addi r7,r0,1
    };
    const auto result = cybertron::ppc::run_program(program, 0U, 64U);
    const bool passed = result.ok && result.executed == 7U &&
        result.cpu.gpr[5] == 12U && result.cpu.gpr[6] == 12U &&
        result.cpu.gpr[7] == 1U && result.cpu.pc == 32U &&
        result.cpu.memory.size() == cybertron::ppc::kDefaultGuestMemorySize;

    std::ostringstream out;
    out << "Synthetic PowerPC test: " << (passed ? "PASS" : "FAIL")
        << "\nExecuted: " << result.executed
        << "\nr5 (add): " << result.cpu.gpr[5]
        << "\nr6 (lwz): " << result.cpu.gpr[6]
        << "\nr7 (branch target): " << result.cpu.gpr[7]
        << "\nGuest PC: 0x" << std::hex << result.cpu.pc
        << "\nGuest RAM: " << result.cpu.memory.size() / (1024U * 1024U)
        << " MiB initially mapped (expandable to 512 MiB)"
        << "\nMemory model: contiguous, big-endian 32-bit accesses"
        << "\nImplemented subset: addi, addis, ori, oris, add, lwz, stw, b"
        << "\nTarget ABI: arm64-v8a";
    if (!result.ok) out << "\nRuntime error: " << result.error;
    if (!passed) out << "\nExpected executed=7, r5=12, r6=12, r7=1, PC=0x20";
    return out.str();
}
} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_cybertron_recomplabs_MainActivity_runSelfTest(JNIEnv* env, jobject) {
    const std::string output = run_synthetic_self_test();
    return env->NewStringUTF(output.c_str());
}
