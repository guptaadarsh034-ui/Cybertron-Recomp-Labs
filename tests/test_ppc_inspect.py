import unittest

from tools.ppc_inspect import decode_word, parse_word, sign_extend


class PowerPCInspectorTests(unittest.TestCase):
    def test_addi(self):
        self.assertEqual(decode_word(0x38600001), "addi r3, r0, 1")

    def test_signed_immediate(self):
        self.assertEqual(decode_word(0x9061FFFC), "stw r3, -4(r1)")

    def test_relative_branch_target(self):
        self.assertEqual(
            decode_word(0x48000008, address=0x100),
            "b 0x00000108 (AA=0, LK=0)",
        )

    def test_nop(self):
        self.assertEqual(decode_word(0x60000000), "nop")

    def test_unknown_instruction_is_preserved(self):
        self.assertIn(".long 0x00000000", decode_word(0x00000000))

    def test_sign_extension(self):
        self.assertEqual(sign_extend(0xFFFF, 16), -1)
        self.assertEqual(sign_extend(0x7FFF, 16), 32767)

    def test_hex_parser(self):
        self.assertEqual(parse_word("0x3860_0001"), 0x38600001)

    def test_rejects_out_of_range_word(self):
        with self.assertRaises(ValueError):
            decode_word(0x1_0000_0000)


if __name__ == "__main__":
    unittest.main()
