#include <jni.h>
#include <array>
#include <cstdint>
#include <iomanip>
#include <sstream>
#include <string>

namespace {
constexpr std::size_t kGuestMemorySize = 256;

struct CpuState {
    std::array<std::uint32_t, 32> gpr{};
    std::array<std::uint8_t, kGuestMemorySize> memory{};
    std::uint32_t pc = 0;
    std::uint32_t lr = 0;
};

bool guest_address(CpuState& cpu, std::uint32_t base, std::int16_t displacement,
                   std::uint32_t& address, std::string& error) {
    const std::int64_t calculated = static_cast<std::int64_t>(base) + displacement;
    if (calculated < 0 || calculated + 4 > static_cast<std::int64_t>(cpu.memory.size())) {
        std::ostringstream message;
        message << "Guest memory access out of range at 0x" << std::hex << calculated;
        error = message.str();
        return false;
    }
    if ((calculated & 3) != 0) {
        error = "Unaligned 32-bit guest memory access";
        return false;
    }
    address = static_cast<std::uint32_t>(calculated);
    return true;
}

bool execute_one(CpuState& cpu, std::uint32_t instruction, std::string& error) {
    const std::uint32_t opcode = instruction >> 26;
    const std::uint32_t rt = (instruction >> 21) & 31U;
    const std::uint32_t ra = (instruction >> 16) & 31U;
    const std::uint32_t rb = (instruction >> 11) & 31U;
    const std::uint32_t xo = (instruction >> 1) & 0x3ffU;
    const auto immediate = static_cast<std::int16_t>(instruction & 0xffffU);
    const std::uint32_t base = ra == 0 ? 0U : cpu.gpr[ra];

    cpu.pc += 4;
    switch (opcode) {
        case 14: // addi
            cpu.gpr[rt] = base + static_cast<std::int32_t>(immediate);
            return true;
        case 24: // ori
            cpu.gpr[ra] = cpu.gpr[rt] | (instruction & 0xffffU);
            return true;
        case 32: // lwz
        case 36: { // stw
            std::uint32_t address = 0;
            if (!guest_address(cpu, base, immediate, address, error)) return false;
            if (opcode == 32) {
                cpu.gpr[rt] = (static_cast<std::uint32_t>(cpu.memory[address]) << 24) |
                              (static_cast<std::uint32_t>(cpu.memory[address + 1]) << 16) |
                              (static_cast<std::uint32_t>(cpu.memory[address + 2]) << 8) |
                              static_cast<std::uint32_t>(cpu.memory[address + 3]);
            } else {
                const std::uint32_t value = cpu.gpr[rt];
                cpu.memory[address] = static_cast<std::uint8_t>(value >> 24);
                cpu.memory[address + 1] = static_cast<std::uint8_t>(value >> 16);
                cpu.memory[address + 2] = static_cast<std::uint8_t>(value >> 8);
                cpu.memory[address + 3] = static_cast<std::uint8_t>(value);
            }
            return true;
        }
        case 18: { // b (unconditional branch)
            std::int32_t displacement = static_cast<std::int32_t>(instruction & 0x03fffffcU);
            if ((displacement & 0x02000000) != 0) displacement |= static_cast<std::int32_t>(0xfc000000U);
            const std::uint32_t target = (instruction & 2U)
                ? static_cast<std::uint32_t>(displacement)
                : static_cast<std::uint32_t>(static_cast<std::int64_t>(cpu.pc - 4) + displacement);
            if ((instruction & 1U) != 0) cpu.lr = cpu.pc;
            cpu.pc = target;
            return true;
        }
        case 31:
            if (xo == 266U) { // add
                cpu.gpr[rt] = cpu.gpr[ra] + cpu.gpr[rb];
                return true;
            }
            break;
        default: break;
    }
    cpu.pc -= 4;
    std::ostringstream message;
    message << "Unsupported instruction 0x" << std::hex << std::setw(8)
            << std::setfill('0') << instruction << " at guest PC 0x" << std::setw(8) << cpu.pc;
    error = message.str();
    return false;
}

std::string run_self_test() {
    // Synthetic only: arithmetic, big-endian store/load, and a branch over an invalid word.
    constexpr std::array<std::uint32_t, 8> program = {
        0x38600007U, // addi r3, r0, 7
        0x38800005U, // addi r4, r0, 5
        0x7ca32214U, // add r5, r3, r4
        0x90a00010U, // stw r5, 16(r0)
        0x80c00010U, // lwz r6, 16(r0)
        0x48000008U, // b +8 (skip word 6)
        0x00000000U, // deliberately unsupported; must be skipped
        0x38e00001U  // addi r7, r0, 1
    };
    CpuState cpu{};
    std::uint32_t executed = 0;
    constexpr std::uint32_t kMaxInstructions = 32;
    while (cpu.pc < program.size() * 4 && executed < kMaxInstructions) {
        const std::uint32_t instruction = program[cpu.pc / 4];
        std::string error;
        if (!execute_one(cpu, instruction, error)) return "FAIL: " + error;
        ++executed;
    }
    const bool passed = cpu.gpr[5] == 12U && cpu.gpr[6] == 12U &&
                        cpu.gpr[7] == 1U && executed == 7U;
    std::ostringstream output;
    output << "Synthetic PowerPC test: " << (passed ? "PASS" : "FAIL")
           << "\nExecuted " << executed << " guest instructions"
           << "\nr5 = " << cpu.gpr[5]
           << "\nr6 (lwz) = " << cpu.gpr[6]
           << "\nr7 (branch target) = " << cpu.gpr[7]
           << "\nGuest PC = 0x" << std::hex << cpu.pc
           << "\nTarget ABI: arm64-v8a";
    if (!passed) output << "\nExpected r5=12, r6=12, r7=1, executed=7";
    return output.str();
}
} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_cybertron_recomplabs_MainActivity_runSelfTest(JNIEnv* env, jobject) {
    const std::string result = run_self_test();
    return env->NewStringUTF(result.c_str());
}
