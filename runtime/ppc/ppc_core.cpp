#include "ppc_core.h"

#include <iomanip>
#include <sstream>

namespace cybertron::ppc {
namespace {

std::int32_t sign_extend_16(std::uint32_t value) {
    return static_cast<std::int16_t>(value & 0xffffU);
}

std::int32_t sign_extend_26(std::uint32_t value) {
    value &= 0x03fffffcU;
    if ((value & 0x02000000U) != 0) {
        value |= 0xfc000000U;
    }
    return static_cast<std::int32_t>(value);
}

std::string unsupported_message(std::uint32_t pc, std::uint32_t word) {
    std::ostringstream out;
    out << "unsupported instruction 0x" << std::hex << std::setw(8)
        << std::setfill('0') << word << " at guest address 0x" << pc;
    return out.str();
}

} // namespace

Interpreter::Interpreter(std::uint32_t load_address)
    : load_address_(load_address) {}

bool Interpreter::step(const std::vector<std::uint32_t>& words,
                       CpuState& state,
                       std::string& error) const {
    if ((state.pc & 3U) != 0 || state.pc < load_address_) {
        error = "program counter is outside the supported aligned code range";
        return false;
    }

    const std::uint32_t index = (state.pc - load_address_) / 4U;
    if (index >= words.size()) {
        return true; // Reaching the end of the supplied test program halts it.
    }

    const std::uint32_t instruction = words[index];
    const std::uint32_t opcode = instruction >> 26U;
    const std::uint32_t rt = (instruction >> 21U) & 31U;
    const std::uint32_t ra = (instruction >> 16U) & 31U;
    const std::uint32_t rb = (instruction >> 11U) & 31U;
    const std::uint32_t next_pc = state.pc + 4U;
    const std::int32_t immediate = sign_extend_16(instruction);

    switch (opcode) {
    case 14: // addi
        state.gpr[rt] = (ra == 0 ? 0U : state.gpr[ra])
                      + static_cast<std::uint32_t>(immediate);
        break;
    case 15: // addis
        state.gpr[rt] = (ra == 0 ? 0U : state.gpr[ra])
                      + (static_cast<std::uint32_t>(immediate) << 16U);
        break;
    case 24: // ori
        state.gpr[ra] = state.gpr[rt] | (instruction & 0xffffU);
        break;
    case 31: { // A deliberately narrow extended-opcode subset.
        const std::uint32_t xo = (instruction >> 1U) & 0x3ffU;
        if (xo == 266U) { // add
            state.gpr[rt] = state.gpr[ra] + state.gpr[rb];
        } else {
            error = unsupported_message(state.pc, instruction);
            return false;
        }
        break;
    }
    case 18: { // b (relative and absolute forms)
        const bool absolute = (instruction & 2U) != 0;
        const std::int32_t displacement = sign_extend_26(instruction);
        const std::uint32_t target = absolute
            ? static_cast<std::uint32_t>(displacement)
            : state.pc + static_cast<std::uint32_t>(displacement);
        state.pc = target;
        if ((instruction & 1U) != 0) {
            state.gpr[0] = state.gpr[0]; // LK is not modeled as a link register yet.
        }
        ++state.steps;
        return true;
    }
    default:
        error = unsupported_message(state.pc, instruction);
        return false;
    }

    state.pc = next_pc;
    ++state.steps;
    return true;
}

RunResult Interpreter::run(const std::vector<std::uint32_t>& words,
                           std::uint64_t max_steps) const {
    RunResult result;
    result.state.pc = load_address_;

    while (result.state.steps < max_steps) {
        if (result.state.pc >= load_address_
            && ((result.state.pc - load_address_) / 4U) >= words.size()) {
            result.ok = true;
            result.message = "program reached the end of the supplied instruction array";
            return result;
        }

        if (!step(words, result.state, result.message)) {
            result.ok = false;
            return result;
        }
    }

    result.ok = false;
    result.message = "execution stopped at the instruction-step limit";
    return result;
}

} // namespace cybertron::ppc
