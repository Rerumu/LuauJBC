package bytecode

sealed class Constant

object Nil : Constant()
object True : Constant()
object False : Constant()

data class Number(val value: Double) : Constant()
data class StringRef(val ref: Int) : Constant()
data class ClosureRef(val ref: Int) : Constant()
data class ImportRef(val ref: Int) : Constant()
data class Table(val data: List<Int>) : Constant()

data class Local(
    val name: Int,
    val startPc: Int,
    val endPc: Int,
    val register: Int
)

data class MetaData(
    val maxStackSize: Int,
    val numParameter: Int,
    val numUpValue: Int,
    val isVararg: Boolean,
)

data class DebugInfo(
    val lineDefined: Int,
    val name: Int,
    val lineInfo: List<Int>,
    val localList: List<Local>,
    val upValueList: List<Int>,
)

data class Function(
    val metaData: MetaData,
    val instructList: List<Int>,
    val constantList: List<Constant>,
    val functionList: List<Int>,
    val debugInfo: DebugInfo,
)

data class Module(
    val stringList: List<ByteArray>,
    val functionList: List<Function>,
    val entryPoint: Int
)
