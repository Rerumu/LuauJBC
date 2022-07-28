package codegen.appender

import bytecode.Constant
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

    private fun genNil() {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.NIL.name, "INSTANCE", TypeCache.NIL.parameter)
    }

    private fun genBoolean(name: String) {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.BOOLEAN.name, name, TypeCache.BOOLEAN.parameter)
    }

    private fun genNumber(value: Double) {
        this.visitor.visitTypeInsn(Opcodes.NEW, TypeCache.NUMBER.name)
        this.visitor.visitInsn(Opcodes.DUP)
        this.visitor.visitLdcInsn(value)
        this.visitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            TypeCache.NUMBER.name,
            "<init>",
            MethodInfo.NUMBER_CONSTRUCTOR,
            false
        )
    }

    private fun genString(index: Int) {
        if (index == 0) {
            this.visitor.visitInsn(Opcodes.ACONST_NULL)
        } else {
            val name = "data$" + (index - 1)

            this.visitor.visitFieldInsn(Opcodes.GETSTATIC, "luau/StringCache", name, TypeCache.STRING.parameter)
        }
    }

    private fun genClosure(index: Int) {
        val name = "luau/Func$$index"

        this.visitor.visitTypeInsn(Opcodes.NEW, name)
        this.visitor.visitInsn(Opcodes.DUP)
        this.visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", MethodInfo.CLOSURE_CONSTRUCTOR, false)
    }

    private fun genGetConstant(index: Int) {
        val descriptor = TypeCache.fromConstant(data[index]).parameter

        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, this.owner, "data$$index", descriptor)
    }

    private fun genTable(keyList: List<Int>) {
        this.visitor.visitTypeInsn(Opcodes.NEW, TypeCache.TABLE.name)
        this.visitor.visitInsn(Opcodes.DUP)
        this.visitor.visitLdcInsn(keyList.size)
        this.visitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            TypeCache.TABLE.name,
            "<init>",
            MethodInfo.TABLE_CONSTRUCTOR,
            false
        )

        for (key in keyList) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.genGetConstant(key)
            this.visitor.visitInsn(Opcodes.ACONST_NULL)
            this.visitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                TypeCache.TABLE.name,
                "set_field",
                MethodInfo.TABLE_SET_FIELD,
                false
            )
        }
    }

    // TODO: Have environment so we can generate `ImportRef`s
    private fun genConstant(const: Constant) {
        when (const) {
            bytecode.Nil -> this.genNil()
            bytecode.True -> this.genBoolean("TRUE")
            bytecode.False -> this.genBoolean("FALSE")
            is bytecode.Number -> this.genNumber(const.value)
            is bytecode.StringRef -> this.genString(const.ref)
            is bytecode.ClosureRef -> this.genClosure(const.ref)
            is bytecode.ImportRef -> this.visitor.visitInsn(Opcodes.ACONST_NULL)
            is bytecode.Table -> this.genTable(const.data)
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

            this.genConstant(value)
            visitor.visitFieldInsn(Opcodes.PUTSTATIC, this.owner, "data$$index", descriptor)
        }

        return ByteCodeAppender.Size(1, 0)
    }
}
