package reader

sealed class Constant

object Nil : Constant()
object True : Constant()
object False : Constant()

data class Number(val value: Double) : Constant()
data class StringRef(val value: ULong) : Constant()
data class ClosureRef(val value: ULong) : Constant()
data class ImportRef(val value: UInt) : Constant()
data class Table(val value: List<ULong>) : Constant()

data class Local(
    val name: ULong,
    val startPc: ULong,
    val endPc: ULong,
    val register: UByte
)

data class MetaData(
    val maxStackSize: UInt,
    val numParameter: UInt,
    val numUpValue: UInt,
    val isVararg: Boolean,
)

data class DebugInfo(
    val debugName: ULong,
    val lineInfo: List<UInt>,
    val localList: List<Local>,
    val upValueList: List<ULong>,
)

data class Function(
    val metaData: MetaData,
    val instructList: List<UInt>,
    val constantList: List<Constant>,
    val functionList: List<ULong>,
    val debugInfo: DebugInfo,
)

data class Module(
    val stringList: List<ByteArray>,
    val functionList: List<Function>,
    val entryPoint: ULong
)
