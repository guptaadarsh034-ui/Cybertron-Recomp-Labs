#include <jni.h>
#include <array>
#include <cstdint>
#include <iomanip>
#include <sstream>
#include <string>

namespace {
struct CpuState { std::array<std::uint32_t, 32> gpr{}; std::uint32_t pc = 0; };

bool execute_one(CpuState& cpu, std::uint32_t instruction, std::string& error) {
    const std::uint32_t opcode = instruction >> 26;
    const std::uint32_t rt = (instruction >> 21) & 31U;
    const std::uint32_t ra = (instruction >> 16) & 31U;
    const std::uint32_t rb = (instruction >> 11) & 31U;
    const std::uint32_t xo = (instruction >> 1) & 0x3ffU;
    const auto immediate = static_cast<std::int16_t>(instruction & 0xffffU);

    switch (opcode) {
        case 14: // addi
            cpu.gpr[rt] = (ra == 0 ? 0U : cpu.gpr[ra]) +
                          static_cast<std::int32_t>(immediate);
            return true;
        case 24: // ori
            cpu.gpr[ra] = cpu.gpr[rt] | (instruction & 0xffffU);
            return true;
        case 31:
            if (xo == 266U) { // add
                cpu.gpr[rt] = cpu.gpr[ra] + cpu.gpr[rb];
                return true;
            }
            break;
        default: break;
    }
    std::ostringstream message;
    message << "Unsupported instruction 0x" << std::hex << std::setw(8)
            << std::setfill('0') << instruction << " at guest PC 0x" << std::setw(8) << cpu.pc;
    error = message.str();
    return false;
}

std::string run_self_test() {
    // Synthetic guest program: r3=7; r4=5; r5=r3+r4. Expected r5=12.
    constexpr std::array<std::uint32_t, 3> program = {0x38600007U, 0x38800005U, 0x7ca32214U};
    CpuState cpu{};
    for (const auto instruction : program) {
        std::string error;
        if (!execute_one(cpu, instruction, error)) return "FAIL: " + error;
        cpu.pc += 4;
    }
    std::ostringstream output;
    output << "Synthetic PowerPC test: " << (cpu.gpr[5] == 12U ? "PASS" : "FAIL")
           << "\\nExecuted 3 guest instructions"
           << "\\nr5 = " << cpu.gpr[5]
           << "\\nGuest PC = 0x" << std::hex << cpu.pc
           << "\\nTarget ABI: arm64-v8a";
    if (cpu.gpr[5] != 12U) output << "\\nExpected r5 = 12";
    return output.str();
}
} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_cybertron_recomplabs_MainActivity_runSelfTest(JNIEnv* env, jobject) {
    const std::string result = run_self_test();
    return env->NewStringUTF(result.c_str());
}
