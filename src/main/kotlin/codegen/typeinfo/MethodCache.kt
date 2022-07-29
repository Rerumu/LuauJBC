package codegen.typeinfo

data class MethodInfo(val name: String, val descriptor: String)

object MethodCache {
    val NUMBER_CONSTRUCTOR = MethodInfo("<init>", "(D)V")
    val STRING_CONSTRUCTOR = MethodInfo("<init>", "(Ljava/lang/String;)V")
    val CLOSURE_CONSTRUCTOR = MethodInfo("<init>", "()V")
    val TABLE_CONSTRUCTOR = MethodInfo("<init>", "(I)V")
    
    val TABLE_SET_FIELD = MethodInfo("set_field", "(Ltypes/ValueType;Ltypes/ValueType;)V")
}
