package codegen.appender

import bytecode.Function
import bytecode.InstructionDecoder
import bytecode.Opcode
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes

class CodeAppender(function: Function) : ByteCodeAppender {
    private val metaData = function.metaData
    private val instructList = function.instructList

    private lateinit var owner: String
    private lateinit var visitor: MethodVisitor

    private fun addInstruction(decoder: InstructionDecoder) {
        when (decoder.getOp()) {
            Opcode.Return -> {
                this.visitor.visitInsn(Opcodes.ACONST_NULL)
                this.visitor.visitInsn(Opcodes.ARETURN)
            }
            else -> this.visitor.visitInsn(Opcodes.NOP)
        }
    }

    override fun apply(
        visitor: MethodVisitor,
        context: Implementation.Context,
        instrumented: MethodDescription
    ): ByteCodeAppender.Size {
        val decoder = InstructionDecoder()
        var index = 0

        this.owner = instrumented.declaringType.asErasure().internalName
        this.visitor = visitor

        while (index < this.instructList.size) {
            decoder.load(this.instructList[index])

            this.addInstruction(decoder)

            index += decoder.getOp().length()
        }

        return ByteCodeAppender.Size(1, this.metaData.maxStackSize)
    }
}
