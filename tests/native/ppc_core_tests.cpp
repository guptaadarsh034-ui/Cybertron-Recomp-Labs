#include "ppc_core.hpp"

#include <cassert>
#include <cstdint>
#include <iostream>
#include <string>
#include <vector>

using cybertron::ppc::CpuState;
using cybertron::ppc::execute_one;
using cybertron::ppc::run_program;

int main() {
    {
        const std::vector<std::uint32_t> program = {
            0x38600007U, 0x38800005U, 0x7ca32214U,
            0x90a00010U, 0x80c00010U, 0x48000008U,
            0x00000000U, 0x38e00001U
        };
        const auto result = run_program(program, 0U, 64U);
        assert(result.ok && result.executed == 7U);
        assert(result.cpu.gpr[5] == 12U && result.cpu.gpr[6] == 12U);
        assert(result.cpu.gpr[7] == 1U && result.cpu.pc == 32U);
    }
    {
        CpuState cpu;
        const auto result = execute_one(cpu, 0x3860ffffU); // addi r3,r0,-1
        assert(result.ok && cpu.gpr[3] == 0xffffffffU && cpu.pc == 4U);
    }
    {
        CpuState cpu;
        const auto result = execute_one(cpu, 0x80000001U); // lwz r0,1(r0)
        assert(!result.ok);
        assert(result.error.find("Unaligned") != std::string::npos);
        assert(cpu.pc == 0U);
    }
    {
        const std::vector<std::uint32_t> loop = {0x48000000U}; // b to self
        const auto result = run_program(loop, 0U, 5U);
        assert(!result.ok && result.executed == 5U);
        assert(result.error.find("Instruction limit") != std::string::npos);
    }
    {
        const std::vector<std::uint32_t> bad = {0x00000000U};
        const auto result = run_program(bad);
        assert(!result.ok);
        assert(result.error.find("Unsupported") != std::string::npos);
    }
    std::cout << "PowerPC core tests: PASS (5 cases)\n";
    return 0;
}
