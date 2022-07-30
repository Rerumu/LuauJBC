package codegen.appender

import codegen.typeinfo.TypeCache
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes
import types.ValueType
import java.lang.reflect.Type

class UpValueAppender(private val length: Int) : ByteCodeAppender {
    private lateinit var owner: String
    private lateinit var visitor: MethodVisitor

    fun getParameterList(): List<Type> {
        return (0 until length).map { ValueType::class.java }
    }

    private fun getRegister(local: Int) {
        this.visitor.visitVarInsn(Opcodes.ALOAD, local + 1)
    }

    private fun putSelfField(name: String, descriptor: String) {
        this.visitor.visitFieldInsn(Opcodes.PUTFIELD, this.owner, name, descriptor)
    }

    override fun apply(
        visitor: MethodVisitor,
        content: Implementation.Context,
        instrumented: MethodDescription
    ): ByteCodeAppender.Size {
        this.owner = instrumented.declaringType.asErasure().internalName
        this.visitor = visitor

        for (i in 0 until this.length) {
            this.getRegister(-1)
            this.getRegister(i)
            this.putSelfField("up$$i", TypeCache.VALUE.parameter)
        }

        this.visitor.visitInsn(Opcodes.RETURN)

        return ByteCodeAppender.Size(1, length + 1)
    }
}
