package codegen.appender

import bytecode.Constant
import codegen.typeinfo.MethodCache
import codegen.typeinfo.MethodInfo
import codegen.typeinfo.TypeCache
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes

class DataAppender(private val data: List<Constant>) : ByteCodeAppender {
    private lateinit var owner: String
    private lateinit var visitor: MethodVisitor

    private fun pushObject(name: String) {
        this.visitor.visitTypeInsn(Opcodes.NEW, name)
        this.visitor.visitInsn(Opcodes.DUP)
    }

    private fun callConstructor(name: String, method: MethodInfo) {
        this.visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, name, method.name, method.descriptor, false)
    }

    private fun pushNil() {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.NIL.name, "INSTANCE", TypeCache.NIL.parameter)
    }

    private fun pushBoolean(name: String) {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.BOOLEAN.name, name, TypeCache.BOOLEAN.parameter)
    }

    private fun pushNumber(value: Double) {
        this.pushObject(TypeCache.NUMBER.name)
        this.visitor.visitLdcInsn(value)
        this.callConstructor(TypeCache.NUMBER.name, MethodCache.NUMBER_CONSTRUCTOR)
    }

    private fun pushString(index: Int) {
        if (index == 0) {
            this.visitor.visitInsn(Opcodes.ACONST_NULL)
        } else {
            val name = "data$" + (index - 1)

            this.visitor.visitFieldInsn(Opcodes.GETSTATIC, "luau/StringCache", name, TypeCache.STRING.parameter)
        }
    }

    private fun pushClosure(index: Int) {
        val name = "luau/Func$$index"

        this.pushObject(name)
        this.callConstructor(name, MethodCache.CLOSURE_CONSTRUCTOR)
    }

    private fun pushConstantIndex(index: Int) {
        val descriptor = TypeCache.fromConstant(data[index]).parameter

        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, this.owner, "data$$index", descriptor)
    }

    private fun pushTable(keyList: List<Int>) {
        val cached = TypeCache.TABLE.name
        val tableSet = MethodCache.TABLE_SET_FIELD

        this.pushObject(cached)
        this.visitor.visitLdcInsn(keyList.size)
        this.callConstructor(cached, MethodCache.TABLE_CONSTRUCTOR)

        for (key in keyList) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.pushConstantIndex(key)
            this.visitor.visitInsn(Opcodes.ACONST_NULL)
            this.visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cached, tableSet.name, tableSet.descriptor, false)
        }
    }

    // TODO: Have environment so we can generate `ImportRef`s
    private fun pushConstant(const: Constant) {
        when (const) {
            bytecode.Nil -> this.pushNil()
            bytecode.True -> this.pushBoolean("TRUE")
            bytecode.False -> this.pushBoolean("FALSE")
            is bytecode.Number -> this.pushNumber(const.value)
            is bytecode.StringRef -> this.pushString(const.ref)
            is bytecode.ClosureRef -> this.pushClosure(const.ref)
            is bytecode.ImportRef -> this.visitor.visitInsn(Opcodes.ACONST_NULL)
            is bytecode.Table -> this.pushTable(const.data)
        }
    }

    override fun apply(
        visitor: MethodVisitor,
        content: Implementation.Context,
        instrumented: MethodDescription
    ): ByteCodeAppender.Size {
        this.owner = instrumented.declaringType.asErasure().internalName
        this.visitor = visitor

        this.data.forEachIndexed { index, value ->
            val descriptor = TypeCache.fromConstant(value).parameter

            this.pushConstant(value)
            visitor.visitFieldInsn(Opcodes.PUTSTATIC, this.owner, "data$$index", descriptor)
        }

        return ByteCodeAppender.Size(1, 0)
    }
}
