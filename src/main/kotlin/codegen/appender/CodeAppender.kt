package codegen.appender

import bytecode.Function
import bytecode.InstructionDecoder
import bytecode.Local
import bytecode.Opcode
import codegen.StringResolver
import codegen.typeinfo.TypeCache
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.Label
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes

private data class CodeBlock(
    val instructList: List<Int>,
    val start: Int,
    val label: Label
)

private object BlockBuilder {
    private fun fillFromCode(instructList: List<Int>, edgeList: MutableList<Int>) {
        val decoder = InstructionDecoder()
        var index = 0

        while (decoder.load(instructList, index)) {
            val op = decoder.getOp()
            val length = op.length()

            when (op) {
                Opcode.LoadBoolean -> {
                    edgeList.add(index + 1)
                    edgeList.add(index + 1 + decoder.getC())
                }

                Opcode.Return -> edgeList.add(index + 1)
                Opcode.Jump, Opcode.JumpSafe -> {
                    edgeList.add(index + 1)
                    edgeList.add(index + 1 + decoder.getD())
                }

                Opcode.JumpEx -> {
                    edgeList.add(index + 1)
                    edgeList.add(index + 1 + decoder.getE())
                }

                Opcode.JumpIfTruthy,
                Opcode.JumpIfFalsy,
                Opcode.JumpIfEqual,
                Opcode.JumpIfNotEqual,
                Opcode.JumpIfLessThan,
                Opcode.JumpIfLessEqual,
                Opcode.JumpIfMoreThan,
                Opcode.JumpIfMoreEqual,
                Opcode.ForNumericPrep,
                Opcode.ForGenericPrepINext,
                Opcode.ForGenericPrepNext,
                Opcode.JumpIfConstant,
                Opcode.JumpIfNotConstant -> {
                    edgeList.add(index + length)
                    edgeList.add(index + 1 + decoder.getD())
                }

                Opcode.ForNumericLoop,
                Opcode.ForGenericLoop,
                Opcode.ForGenericLoopINext,
                Opcode.ForGenericLoopNext -> {
                    edgeList.add(index + length)
                    edgeList.add(index + decoder.getD())
                }

                Opcode.FastCall, Opcode.FastCall1, Opcode.FastCall2, Opcode.FastCall2K -> {
                    edgeList.add(index + length)
                    edgeList.add(index + 2 + decoder.getC())
                }

                else -> {}
            }

            index += length
        }
    }

    private fun fillFromLocals(localList: List<Local>, edgeList: MutableList<Int>) {
        for (local in localList) {
            edgeList.add(local.startPc)
            edgeList.add(local.endPc)
        }
    }

    private fun splitEdgeList(instructList: List<Int>, edgeList: List<Int>): List<CodeBlock> {
        var index = 0

        return edgeList.sorted().distinct().map {
            val code = instructList.slice(index until it)
            val block = CodeBlock(code, index, Label())

            index = it
            block
        }
    }

    private fun withLastEdge(prev: List<CodeBlock>): List<CodeBlock> {
        val list = prev.toMutableList()
        val start = list.last().start + list.last().instructList.size

        list.add(CodeBlock(emptyList(), start, Label()))

        return list
    }

    fun getBlockList(instructList: List<Int>, localList: List<Local>): List<CodeBlock> {
        val edgeList = mutableListOf<Int>()

        this.fillFromCode(instructList, edgeList)
        this.fillFromLocals(localList, edgeList)

        val list = this.splitEdgeList(instructList, edgeList)

        return this.withLastEdge(list)
    }
}

private fun findLabelContaining(blockList: List<CodeBlock>, pc: Int): Label {
    val index = blockList.binarySearch { it.start.compareTo(pc) }

    return blockList[index].label
}

class CodeAppender(private val resolver: StringResolver, function: Function) : ByteCodeAppender {
    private val metaData = function.metaData
    private val debugInfo = function.debugInfo
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

    private fun addCodeBlock(block: CodeBlock, decoder: InstructionDecoder) {
        this.visitor.visitLabel(block.label)

        var index = 0

        while (decoder.load(block.instructList, index)) {
            this.addInstruction(decoder)

            index += decoder.getOp().length()
        }
    }

    private fun addLocalInfo(blockList: List<CodeBlock>) {
        for (local in this.debugInfo.localList) {
            val start = findLabelContaining(blockList, local.startPc)
            val end = findLabelContaining(blockList, local.endPc)
            val name = this.resolver.getString(local.name)

            this.visitor.visitLocalVariable(name, TypeCache.VALUE.name, null, start, end, local.register)
        }
    }

    override fun apply(
        visitor: MethodVisitor,
        context: Implementation.Context,
        instrumented: MethodDescription
    ): ByteCodeAppender.Size {
        this.owner = instrumented.declaringType.asErasure().internalName
        this.visitor = visitor

        val blockList = BlockBuilder.getBlockList(this.instructList, this.debugInfo.localList)
        val decoder = InstructionDecoder()

        for (block in blockList) {
            this.addCodeBlock(block, decoder)
        }

        this.addLocalInfo(blockList)

        return ByteCodeAppender.Size(1, this.metaData.maxStackSize)
    }
}
