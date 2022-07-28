package bytecode

class InstructionDecoder {
    private var temporary = 0

    fun load(instruction: Int) {
        this.temporary = instruction
    }

    fun getOp(): Opcode {
        return Opcode.values()[this.temporary and 0xFF]
    }

    fun getA(): Int {
        return this.temporary shr 8 and 0xFF
    }

    fun getB(): Int {
        return this.temporary shr 16 and 0xFF
    }

    fun getC(): Int {
        return this.temporary shr 24 and 0xFF
    }

    fun getD(): Int {
        val temp = this.temporary shr 16 and 0xFFFF

        return if (temp < 0x8000) temp else temp - 0x10000
    }

    fun getE(): Int {
        val temp = this.temporary shr 8 and 0xFFFFFF

        return if (temp < 0x800000) temp else temp - 0x1000000
    }
}
