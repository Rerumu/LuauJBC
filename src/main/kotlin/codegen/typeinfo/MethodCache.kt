package codegen.typeinfo

data class MethodInfo(val name: String, val descriptor: String)

object MethodCache {
    val NUMBER_FROM = MethodInfo("from", "(D)Ltypes/NumberType;")
    val STRING_FROM = MethodInfo("from", "(Ljava/lang/String;)Ltypes/StringType;")
    val TABLE_FROM = MethodInfo("from", "(I)Ltypes/TableType;")
    val TABLE_COPY = MethodInfo("copy", "(Ltypes/TableType;)Ltypes/TableType;")

    val TABLE_SET_FIELD = MethodInfo("setField", "(Ltypes/ValueType;Ltypes/ValueType;)V")
}
