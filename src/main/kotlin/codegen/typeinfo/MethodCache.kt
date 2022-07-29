package codegen.typeinfo

data class MethodInfo(val name: String, val descriptor: String)

object MethodCache {
    val NUMBER_FROM = MethodInfo("from", "(D)Ltypes/NumberType;")
    val STRING_FROM = MethodInfo("from", "(Ljava/lang/String;)Ltypes/StringType;")
    val TABLE_FROM = MethodInfo("from", "(I)Ltypes/TableType;")
    val TABLE_COPY = MethodInfo("copy", "(Ltypes/TableType;)Ltypes/TableType;")

    val TABLE_GET_FIELD = MethodInfo("getField", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val TABLE_SET_FIELD = MethodInfo("setField", "(Ltypes/ValueType;Ltypes/ValueType;)V")

    val CLOSURE_CALL = MethodInfo("call", "([Ltypes/ValueType;)[Ltypes/ValueType;")

    val VALUE_ADD = MethodInfo("add", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_SUB = MethodInfo("sub", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_MUL = MethodInfo("mul", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_DIV = MethodInfo("div", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_MOD = MethodInfo("mod", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_POW = MethodInfo("pow", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_AND = MethodInfo("and", "(Ltypes/ValueType;)Ltypes/ValueType;")
    val VALUE_OR = MethodInfo("or", "(Ltypes/ValueType;)Ltypes/ValueType;")

    val VALUE_NOT = MethodInfo("not", "()Ltypes/ValueType;")
    val VALUE_MINUS = MethodInfo("minus", "()Ltypes/ValueType;")
    val VALUE_LENGTH = MethodInfo("length", "()Ltypes/ValueType;")

    val VALUE_EQUAL = MethodInfo("equals", "(Ltypes/ValueType;)Z")
    val VALUE_COMPARE = MethodInfo("compareTo", "(Ltypes/ValueType;)I")
    val VALUE_BOOLEAN = MethodInfo("toBoolean", "()Z;")

    val VALUE_CONCAT = MethodInfo("concatenate", "([Ltypes/ValueType;)Ltypes/ValueType;")

    val FOR_NUMERIC_TEST = MethodInfo("shouldLoopRun", "(Ltypes/ValueType;Ltypes/ValueType;Ltypes/ValueType;)Z")
}
