#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>

namespace cybertron::ppc {

// Xbox 360 exposes a much larger guest address space than the old 4 KiB
// instruction-demo buffer. Start with a modest allocation and grow only when
// a validated loader requests it.
constexpr std::size_t kDefaultGuestMemorySize = 16U * 1024U * 1024U;
constexpr std::size_t kMaximumGuestMemorySize = 512U * 1024U * 1024U;
constexpr std::uint32_t kInstructionAlignment = 4;

struct CpuState {
    std::array<std::uint32_t, 32> gpr{};
    std::vector<std::uint8_t> memory =
        std::vector<std::uint8_t>(kDefaultGuestMemorySize);
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

// Grow or shrink the contiguous guest RAM allocation within the Xbox 360
// 512 MiB physical-memory ceiling. Newly added bytes are zero-initialized.
bool resize_guest_memory(CpuState& cpu, std::size_t bytes, std::string& error);

StepResult execute_one(CpuState& cpu, std::uint32_t instruction);
RunResult run_program(const std::vector<std::uint32_t>& program,
                      std::uint32_t entry_pc = 0,
                      std::uint32_t instruction_limit = 100000);

} // namespace cybertron::ppc
