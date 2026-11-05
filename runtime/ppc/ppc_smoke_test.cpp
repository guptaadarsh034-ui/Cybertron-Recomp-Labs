#include "ppc_core.h"

#include <iostream>
#include <vector>

int main() {
    // Synthetic PPC program:
    //   addi r3, r0, 40
    //   addi r4, r0, 2
    //   add  r5, r3, r4
    const std::vector<std::uint32_t> program = {
        0x38600028U,
        0x38800002U,
        0x7ca32214U,
    };

    const cybertron::ppc::Interpreter interpreter;
    const auto result = interpreter.run(program);

    if (!result.ok) {
        std::cerr << "PPC smoke test failed: " << result.message << '\n';
        return 1;
    }

    std::cout << "Cybertron Recomp Labs PPC smoke test\n"
              << "r3 = " << result.state.gpr[3] << '\n'
              << "r4 = " << result.state.gpr[4] << '\n'
              << "r5 = " << result.state.gpr[5] << '\n'
              << "instructions executed = " << result.state.steps << '\n';

    if (result.state.gpr[3] != 40U
        || result.state.gpr[4] != 2U
        || result.state.gpr[5] != 42U) {
        std::cerr << "Unexpected register result\n";
        return 2;
    }

    std::cout << "PASS: synthetic PowerPC program executed correctly\n";
    return 0;
}
