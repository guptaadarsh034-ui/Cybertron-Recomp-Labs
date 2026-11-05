#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>

namespace cybertron::ppc {

struct CpuState {
    std::array<std::uint32_t, 32> gpr{};
    std::uint32_t pc = 0; // Byte address relative to the program's load address.
    std::uint64_t steps = 0;
};

struct RunResult {
    bool ok = false;
    std::string message;
    CpuState state;
};

class Interpreter {
public:
    explicit Interpreter(std::uint32_t load_address = 0x1000);

    RunResult run(const std::vector<std::uint32_t>& words,
                  std::uint64_t max_steps = 10000) const;

private:
    bool step(const std::vector<std::uint32_t>& words,
              CpuState& state,
              std::string& error) const;

    std::uint32_t load_address_;
};

} // namespace cybertron::ppc
