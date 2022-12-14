package codegen.typeinfo

import BuiltIn
import bytecode.Constant
import net.bytebuddy.jar.asm.Type
import template.Auxiliary
import template.Program
import types.*

data class TypeInfo(val klass: Class<out Any>) {
    val name = Type.getInternalName(klass)!!
    val parameter = Type.getDescriptor(klass)!!
}

object TypeCache {
    val VALUE = TypeInfo(ValueType::class.java)
    val NIL = TypeInfo(NilType::class.java)
    val BOOLEAN = TypeInfo(BooleanType::class.java)
    val NUMBER = TypeInfo(NumberType::class.java)
    val STRING = TypeInfo(StringType::class.java)
    val CLOSURE = TypeInfo(ClosureType::class.java)
    val TABLE = TypeInfo(TableType::class.java)

    val AUXILIARY = TypeInfo(Auxiliary::class.java)
    val PROGRAM = TypeInfo(Program::class.java)
    val BUILTIN = TypeInfo(BuiltIn::class.java)

    fun fromConstant(constant: Constant): TypeInfo {
        return when (constant) {
            bytecode.Nil -> this.NIL
            bytecode.True -> this.BOOLEAN
            bytecode.False -> this.BOOLEAN
            is bytecode.Number -> this.NUMBER
            is bytecode.StringRef -> this.STRING
            is bytecode.ClosureRef -> this.CLOSURE
            is bytecode.ImportRef -> this.VALUE
            is bytecode.Table -> this.TABLE
        }
    }
}
