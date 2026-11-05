#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>

namespace cybertron::ppc {

constexpr std::size_t kGuestMemorySize = 4096;
constexpr std::uint32_t kInstructionAlignment = 4;

struct CpuState {
    std::array<std::uint32_t, 32> gpr{};
    std::array<std::uint8_t, kGuestMemorySize> memory{};
    std::uint32_t pc = 0;
    std::uint32_t lr = 0;
};

struct StepResult {
    bool ok = false;
    std::string error;
};

struct RunResult {
    bool ok = false;
    std::uint32_t executed = 0;
    CpuState cpu{};
    std::string error;
};

StepResult execute_one(CpuState& cpu, std::uint32_t instruction);
RunResult run_program(const std::vector<std::uint32_t>& program,
                      std::uint32_t entry_pc = 0,
                      std::uint32_t instruction_limit = 100000);

} // namespace cybertron::ppc
