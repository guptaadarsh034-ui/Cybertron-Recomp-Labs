#!/usr/bin/env python3
"""Small, dependency-free Xbox 360 PowerPC word inspector.

This is an educational first step, not a complete PPC/VMX128 disassembler
and not a static recompiler. Input words are 32-bit instruction values written
as hexadecimal, e.g. 38600001. No game files are needed.
"""
from __future__ import annotations

import argparse


def sign_extend(value: int, bits: int) -> int:
    """Interpret the low *bits* of value as a signed two's-complement integer."""
    sign = 1 << (bits - 1)
    return (value & (sign - 1)) - (value & sign)


def decode_word(word: int, address: int = 0) -> str:
    """Return a conservative human-readable description of one PPC instruction."""
    if not 0 <= word <= 0xFFFFFFFF:
        raise ValueError("instruction word must be between 0 and 0xffffffff")

    if word == 0x60000000:
        return "nop"

    op = word >> 26
    rt_rs = (word >> 21) & 31
    ra = (word >> 16) & 31
    rb = (word >> 11) & 31
    imm = sign_extend(word & 0xFFFF, 16)

    if op == 14:
        return f"addi r{rt_rs}, r{ra}, {imm}"
    if op == 15:
        return f"addis r{rt_rs}, r{ra}, {imm}"
    if op == 24:
        return f"ori r{ra}, r{rt_rs}, 0x{word & 0xffff:04x}"
    if op == 25:
        return f"oris r{ra}, r{rt_rs}, 0x{word & 0xffff:04x}"
    if op == 32:
        return f"lwz r{rt_rs}, {imm}(r{ra})"
    if op == 36:
        return f"stw r{rt_rs}, {imm}(r{ra})"
    if op == 18:
        displacement = sign_extend(word & 0x03FFFFFC, 26)
        absolute = bool(word & 2)
        link = bool(word & 1)
        target = displacement if absolute else address + displacement
        return f"b {'0x' + format(target & 0xffffffff, '08x')} (AA={int(absolute)}, LK={int(link)})"
    if op == 16:
        displacement = sign_extend(word & 0x0000FFFC, 16)
        absolute = bool(word & 2)
        link = bool(word & 1)
        target = displacement if absolute else address + displacement
        bo = (word >> 21) & 31
        bi = (word >> 16) & 31
        return f"bc BO={bo}, BI={bi}, 0x{target & 0xffffffff:08x} (AA={int(absolute)}, LK={int(link)})"
    if op == 19 and ((word >> 1) & 0x3ff) == 16:
        bo = (word >> 21) & 31
        bi = (word >> 16) & 31
        return f"bclr BO={bo}, BI={bi}, LK={word & 1}"
    return f".long 0x{word:08x}  # unsupported primary opcode {op}"


def parse_word(text: str) -> int:
    cleaned = text.strip().lower().replace("_", "")
    if cleaned.startswith("0x"):
        cleaned = cleaned[2:]
    try:
        value = int(cleaned, 16)
    except ValueError as exc:
        raise argparse.ArgumentTypeError(f"not a hexadecimal instruction word: {text}") from exc
    if not 0 <= value <= 0xFFFFFFFF:
        raise argparse.ArgumentTypeError("word must fit in 32 bits")
    return value


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("words", nargs="+", type=parse_word, help="32-bit instruction words in hex")
    parser.add_argument("--address", type=lambda x: int(x, 0), default=0,
                        help="address of the first instruction (default: 0)")
    parser.add_argument("--step", type=lambda x: int(x, 0), default=4,
                        help="address increment per word (default: 4)")
    args = parser.parse_args()
    if args.step <= 0:
        parser.error("--step must be positive")
    for index, word in enumerate(args.words):
        address = args.address + index * args.step
        print(f"{address & 0xffffffff:08x}: {word:08x}  {decode_word(word, address)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
