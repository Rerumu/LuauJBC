package reader

import java.nio.ByteBuffer

private const val LUAU_VERSION = 2.toByte()

object BcReader {
    private fun readBoolean(data: ByteBuffer): Boolean {
        return data.get() != 0.toByte()
    }

    private fun readByte(data: ByteBuffer): UByte {
        return data.get().toUByte()
    }

    private fun readInt(data: ByteBuffer): UInt {
        return data.int.toUInt()
    }

    private fun readLong(data: ByteBuffer): ULong {
        return data.long.toULong()
    }

    private fun readAnySize(data: ByteBuffer): ULong {
        var result = 0uL

        for (i in 0 until 8) {
            val value = data.get().toULong()

            result = result or (value and 0x7FuL) shl i * 7

            if (value and 0x80uL == 0uL) {
                break
            }
        }

        return result
    }

    private fun <T> readList(data: ByteBuffer, reader: (ByteBuffer) -> T): List<T> {
        val size = this.readAnySize(data).toInt()

        return (0 until size).map { reader(data) }
    }

    private fun readString(data: ByteBuffer): ByteArray {
        val size = this.readAnySize(data).toInt()
        val array = ByteArray(size)

        data.get(array)

        return array
    }

    private fun readConstant(data: ByteBuffer): Constant {
        return when (this.readByte(data).toInt()) {
            0 -> Nil
            1 -> if (this.readBoolean(data)) True else False
            2 -> Number(data.double)
            3 -> StringRef(this.readAnySize(data))
            4 -> ImportRef(this.readInt(data))
            5 -> Table(this.readList(data, ::readLong))
            6 -> ClosureRef(this.readAnySize(data))
            else -> throw IllegalStateException("Invalid constant type")
        }
    }

    private fun readMetaData(data: ByteBuffer): MetaData {
        return MetaData(
            this.readByte(data).toUInt(),
            this.readByte(data).toUInt(),
            this.readByte(data).toUInt(),
            this.readBoolean(data),
        )
    }

    private fun readLineInfo(data: ByteBuffer, len: Int): List<UInt> {
        val lineGapLog2 = this.readByte(data).toInt()
        var lastOffset = 0u

        val relLineInfo = (0 until len).map {
            lastOffset += this.readByte(data)
            lastOffset.toUByte()
        }

        val intervals = ((len - 1) shr lineGapLog2) + 1
        var lastLine = 0u

        val absLineInfo = (0 until intervals).map {
            lastLine += this.readInt(data)
            lastLine
        }

        return (0 until len).map { absLineInfo[it shr lineGapLog2] + relLineInfo[it] }
    }

    private fun readLocal(data: ByteBuffer): Local {
        return Local(
            this.readAnySize(data),
            this.readAnySize(data),
            this.readAnySize(data),
            this.readByte(data),
        )
    }

    private fun readDebugInfo(data: ByteBuffer, len: Int): DebugInfo {
        val debugName = this.readAnySize(data)
        val lineList = if (this.readBoolean(data))
            this.readLineInfo(data, len)
        else
            emptyList()

        var localList = emptyList<Local>()
        var upValueList = emptyList<ULong>()

        if (this.readBoolean(data)) {
            localList = this.readList(data, ::readLocal)
            upValueList = this.readList(data, ::readAnySize)
        }

        return DebugInfo(
            debugName,
            lineList,
            localList,
            upValueList,
        )
    }

    private fun readFunction(data: ByteBuffer): Function {
        val metaData = this.readMetaData(data)
        val instructList = this.readList(data, ::readInt)
        val constantList = this.readList(data, ::readConstant)
        val functionList = this.readList(data, ::readAnySize)
        val debugInfo = this.readDebugInfo(data, instructList.size)

        return Function(
            metaData,
            instructList,
            constantList,
            functionList,
            debugInfo,
        )
    }

    fun readModule(data: ByteBuffer): Module {
        if (data.get() != LUAU_VERSION) {
            throw IllegalArgumentException("Invalid Luau version")
        }

        return Module(
            this.readList(data, ::readString),
            this.readList(data, ::readFunction),
            this.readAnySize(data)
        )
    }
}
