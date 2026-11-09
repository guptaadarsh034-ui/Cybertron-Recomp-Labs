#include "ppc_core.hpp"

#include <cstdint>
#include <iomanip>
#include <limits>
#include <new>
#include <stdexcept>
#include <sstream>

namespace cybertron::ppc {
namespace {

std::int32_t sign_extend_16(std::uint32_t value) {
    return static_cast<std::int16_t>(value & 0xffffU);
}

bool effective_address(const CpuState& cpu, std::uint32_t ra,
                       std::int32_t displacement, std::uint32_t& address,
                       std::string& error) {
    const std::uint32_t base = ra == 0 ? 0U : cpu.gpr[ra];
    const std::int64_t sum = static_cast<std::int64_t>(base) + displacement;
    if (sum < 0 ||
        cpu.memory.size() < 4U ||
        static_cast<std::uint64_t>(sum) >
            static_cast<std::uint64_t>(cpu.memory.size() - 4U)) {
        std::ostringstream out;
        out << "Guest memory access out of range: 0x" << std::hex << sum
            << " (mapped RAM: " << std::dec << cpu.memory.size() << " bytes)";
        error = out.str();
        return false;
    }
    if ((sum & 3) != 0) {
        error = "Unaligned 32-bit guest memory access";
        return false;
    }
    address = static_cast<std::uint32_t>(sum);
    return true;
}

std::uint32_t sign_extend_branch_26(std::uint32_t instruction) {
    std::uint32_t displacement = instruction & 0x03fffffcU;
    if ((displacement & 0x02000000U) != 0) {
        displacement |= 0xfc000000U;
    }
    return displacement;
}

} // namespace

bool resize_guest_memory(CpuState& cpu, std::size_t bytes, std::string& error) {
    if (bytes == 0U || bytes > kMaximumGuestMemorySize) {
        std::ostringstream out;
        out << "Requested guest RAM size " << bytes
            << " is outside the supported range (1.."
            << kMaximumGuestMemorySize << " bytes)";
        error = out.str();
        return false;
    }
    try {
        cpu.memory.resize(bytes, 0U);
        error.clear();
        return true;
    } catch (const std::bad_alloc&) {
        error = "Not enough host memory to allocate the requested guest RAM";
        return false;
    } catch (const std::length_error&) {
        error = "Requested guest RAM size cannot be represented by this runtime";
        return false;
    }
}

StepResult execute_one(CpuState& cpu, std::uint32_t instruction) {
    if ((cpu.pc & (kInstructionAlignment - 1U)) != 0U) {
        return {false, "Instruction address is not 4-byte aligned"};
    }
    if (cpu.pc > std::numeric_limits<std::uint32_t>::max() - 4U) {
        return {false, "Guest PC overflow"};
    }

    const std::uint32_t current_pc = cpu.pc;
    const std::uint32_t opcode = instruction >> 26U;
    const std::uint32_t rt = (instruction >> 21U) & 31U;
    const std::uint32_t ra = (instruction >> 16U) & 31U;
    const std::uint32_t rb = (instruction >> 11U) & 31U;
    const std::uint32_t xo = (instruction >> 1U) & 0x3ffU;
    const std::int32_t immediate = sign_extend_16(instruction);
    const std::uint32_t base = ra == 0U ? 0U : cpu.gpr[ra];

    cpu.pc = current_pc + 4U;
    switch (opcode) {
        case 14U: // addi RT,RA,SI
            cpu.gpr[rt] = base + static_cast<std::uint32_t>(immediate);
            return {true, {}};

        case 15U: // addis RT,RA,SI (shifted immediate)
            cpu.gpr[rt] = base + (static_cast<std::uint32_t>(immediate) << 16U);
            return {true, {}};

        case 24U: // ori RA,RS,UI
            cpu.gpr[ra] = cpu.gpr[rt] | (instruction & 0xffffU);
            return {true, {}};

        case 25U: // oris RA,RS,UI
            cpu.gpr[ra] = cpu.gpr[rt] | ((instruction & 0xffffU) << 16U);
            return {true, {}};

        case 32U: // lwz RT,D(RA)
        case 36U: { // stw RS,D(RA)
            std::uint32_t address = 0;
            std::string error;
            if (!effective_address(cpu, ra, immediate, address, error)) {
                cpu.pc = current_pc;
                return {false, error};
            }
            if (opcode == 32U) {
                cpu.gpr[rt] =
                    (static_cast<std::uint32_t>(cpu.memory[address]) << 24U) |
                    (static_cast<std::uint32_t>(cpu.memory[address + 1U]) << 16U) |
                    (static_cast<std::uint32_t>(cpu.memory[address + 2U]) << 8U) |
                    static_cast<std::uint32_t>(cpu.memory[address + 3U]);
            } else {
                const std::uint32_t value = cpu.gpr[rt];
                cpu.memory[address] = static_cast<std::uint8_t>(value >> 24U);
                cpu.memory[address + 1U] = static_cast<std::uint8_t>(value >> 16U);
                cpu.memory[address + 2U] = static_cast<std::uint8_t>(value >> 8U);
                cpu.memory[address + 3U] = static_cast<std::uint8_t>(value);
            }
            return {true, {}};
        }

        case 18U: { // b LI,AA,LK
            const std::uint32_t displacement = sign_extend_branch_26(instruction);
            const std::uint32_t target = (instruction & 2U) != 0U
                ? displacement
                : current_pc + displacement;
            if ((instruction & 1U) != 0U) {
                cpu.lr = current_pc + 4U;
            }
            cpu.pc = target;
            return {true, {}};
        }

        case 31U:
            if (xo == 266U) { // add RT,RA,RB (Rc does not affect GPR result)
                cpu.gpr[rt] = cpu.gpr[ra] + cpu.gpr[rb];
                return {true, {}};
            }
            break;

        default:
            break;
    }

    cpu.pc = current_pc;
    std::ostringstream out;
    out << "Unsupported PowerPC instruction 0x" << std::hex
        << std::setw(8) << std::setfill('0') << instruction
        << " at guest PC 0x" << std::setw(8) << current_pc;
    return {false, out.str()};
}

RunResult run_program(const std::vector<std::uint32_t>& program,
                      std::uint32_t entry_pc,
                      std::uint32_t instruction_limit) {
    RunResult result;
    result.cpu.pc = entry_pc;
    if ((entry_pc & 3U) != 0U) {
        result.error = "Entry PC is not 4-byte aligned";
        return result;
    }
    if (program.empty()) {
        result.error = "Guest program is empty";
        return result;
    }
    if (instruction_limit == 0U) {
        result.error = "Instruction limit must be greater than zero";
        return result;
    }
    const std::uint64_t program_bytes = program.size() * 4ULL;
    if (entry_pc >= program_bytes) {
        result.error = "Entry PC is outside the loaded program";
        return result;
    }

    while (result.executed < instruction_limit) {
        if (result.cpu.pc == program_bytes) {
            result.ok = true;
            return result;
        }
        if (result.cpu.pc > program_bytes) {
            result.error = "Guest PC is outside the loaded program";
            return result;
        }
        if ((result.cpu.pc & 3U) != 0U) {
            result.error = "Branch target is not 4-byte aligned";
            return result;
        }
        const std::size_t index = result.cpu.pc / 4U;
        const std::uint32_t instruction = program[index];
        const StepResult step = execute_one(result.cpu, instruction);
        if (!step.ok) {
            result.error = step.error;
            return result;
        }
        ++result.executed;
        if ((instruction >> 26U) == 18U &&
            static_cast<std::uint64_t>(result.cpu.pc) >= program_bytes) {
            result.error = "Unconditional branch target is outside the loaded program";
            return result;
        }
    }
    result.error = "Instruction limit reached (possible guest loop)";
    return result;
}

} // namespace cybertron::ppc
