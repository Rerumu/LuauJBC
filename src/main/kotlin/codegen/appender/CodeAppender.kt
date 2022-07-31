package codegen.appender

import bytecode.Function
import bytecode.InstructionDecoder
import bytecode.Local
import bytecode.Opcode
import codegen.StringResolver
import codegen.typeinfo.MethodCache
import codegen.typeinfo.MethodInfo
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

class CodeAppender(private val resolver: StringResolver, function: Function) : ByteCodeAppender {
    private val metaData = function.metaData
    private val debugInfo = function.debugInfo
    private val instructList = function.instructList
    private val constantList = function.constantList

    private lateinit var owner: String
    private lateinit var visitor: MethodVisitor
    private lateinit var blockList: List<CodeBlock>

    private fun getRegister(local: Int) {
        this.visitor.visitVarInsn(Opcodes.ALOAD, local + 1)
    }

    private fun setRegister(local: Int) {
        this.visitor.visitVarInsn(Opcodes.ASTORE, local + 1)
    }

    private fun pushSelfField(name: String, descriptor: String) {
        this.getRegister(-1)
        this.visitor.visitFieldInsn(Opcodes.GETFIELD, this.owner, name, descriptor)
    }

    private fun putSelfField(name: String, descriptor: String) {
        this.visitor.visitFieldInsn(Opcodes.PUTFIELD, this.owner, name, descriptor)
    }

    private fun pushSelfStatic(name: String, descriptor: String) {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, this.owner, name, descriptor)
    }

    private fun pushEnvironment() {
        val name = TypeCache.BUILTIN.name
        val descriptor = TypeCache.TABLE.parameter

        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, name, "INSTANCE", descriptor)
    }

    private fun callVirtual(name: String, method: MethodInfo) {
        this.visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name, method.name, method.descriptor, false)
    }

    private fun callStatic(name: String, method: MethodInfo) {
        this.visitor.visitMethodInsn(Opcodes.INVOKESTATIC, name, method.name, method.descriptor, false)
    }

    private fun pushInteger(value: Int) {
        this.visitor.visitLdcInsn(value.toDouble())
        this.callStatic(TypeCache.NUMBER.name, MethodCache.NUMBER_FROM)
    }

    private fun pushConstant(index: Int) {
        val constant = this.constantList[index]
        val descriptor = TypeCache.fromConstant(constant).parameter

        this.pushSelfStatic("data$$index", descriptor)
    }

    private fun findLabelContaining(position: Int): Label {
        val index = this.blockList.binarySearch { it.start.compareTo(position) }

        return this.blockList[index].label
    }

    private fun jumpOperation(condition: Int, position: Int, offset: Int) {
        val label = this.findLabelContaining(position + offset + 1)

        this.visitor.visitJumpInsn(condition, label)
    }

    private fun jumpAlways(position: Int, offset: Int) {
        this.jumpOperation(Opcodes.GOTO, position, offset)
    }

    private fun jumpIf(decoder: InstructionDecoder, position: Int, condition: Int) {
        this.getRegister(decoder.getA())
        this.callVirtual(TypeCache.VALUE.name, MethodCache.VALUE_BOOLEAN)
        this.jumpOperation(condition, position, decoder.getD())
    }

    private fun jumpEqual(decoder: InstructionDecoder, position: Int, condition: Int) {
        this.callVirtual(TypeCache.VALUE.name, MethodCache.VALUE_EQUAL)
        this.jumpOperation(condition, position, decoder.getD())
    }

    private fun jumpCompare(decoder: InstructionDecoder, position: Int, condition: Int) {
        this.getRegister(decoder.getA())
        this.getRegister(this.instructList[position + 1])
        this.callVirtual(TypeCache.VALUE.name, MethodCache.VALUE_COMPARE)
        this.jumpOperation(condition, position, decoder.getD())
    }

    private fun loadNil(local: Int) {
        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.NIL.name, "INSTANCE", TypeCache.NIL.parameter)
        this.setRegister(local)
    }

    private fun loadBoolean(decoder: InstructionDecoder, position: Int) {
        val name = if (decoder.getB() != 0) "TRUE" else "FALSE"
        val skip = decoder.getC()

        this.visitor.visitFieldInsn(Opcodes.GETSTATIC, TypeCache.BOOLEAN.name, name, TypeCache.BOOLEAN.parameter)
        this.setRegister(decoder.getA())

        if (skip != 0) {
            this.jumpAlways(position, skip)
        }
    }

    private fun loadUpValue(decoder: InstructionDecoder) {
        val name = "up$" + decoder.getB()

        this.pushSelfField(name, TypeCache.VALUE.parameter)
        this.setRegister(decoder.getA())
    }

    private fun storeUpValue(decoder: InstructionDecoder) {
        val name = "up$" + decoder.getB()

        this.getRegister(-1)
        this.getRegister(decoder.getA())
        this.putSelfField(name, TypeCache.VALUE.parameter)
    }

    private fun loadImport(decoder: InstructionDecoder, position: Int) {
        val data = this.instructList[position + 1]

        this.pushEnvironment()

        for (i in 0 until (data ushr 30)) {
            val id = data shr (20 - i * 10) and 0x3FF

            this.pushConstant(id)
            this.callVirtual(TypeCache.TABLE.name, MethodCache.TABLE_GET_FIELD)
        }

        this.setRegister(decoder.getA())
    }

    private fun loadGlobal(decoder: InstructionDecoder, position: Int) {
        val index = this.instructList[position + 1]

        this.pushEnvironment()
        this.pushSelfStatic("data$$index", TypeCache.STRING.parameter)
        this.callVirtual(TypeCache.TABLE.name, MethodCache.TABLE_GET_FIELD)
        this.setRegister(decoder.getA())
    }

    private fun storeGlobal(decoder: InstructionDecoder, position: Int) {
        val index = this.instructList[position + 1]

        this.getRegister(decoder.getA())
        this.pushEnvironment()
        this.pushSelfStatic("data$$index", TypeCache.STRING.parameter)
        this.callVirtual(TypeCache.TABLE.name, MethodCache.TABLE_SET_FIELD)
    }

    private fun loadTableField(decoder: InstructionDecoder) {
        this.getRegister(decoder.getB())
        this.getRegister(decoder.getC())
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_GET_FIELD)
        this.setRegister(decoder.getA())
    }

    private fun storeTableField(decoder: InstructionDecoder) {
        this.getRegister(decoder.getB())
        this.getRegister(decoder.getC())
        this.getRegister(decoder.getA())
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_SET_FIELD)
    }

    private fun loadTableFieldK(decoder: InstructionDecoder, position: Int) {
        this.getRegister(decoder.getB())
        this.pushConstant(this.instructList[position + 1])
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_GET_FIELD)
        this.setRegister(decoder.getA())
    }

    private fun storeTableFieldK(decoder: InstructionDecoder, position: Int) {
        this.getRegister(decoder.getB())
        this.pushConstant(this.instructList[position + 1])
        this.getRegister(decoder.getA())
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_SET_FIELD)
    }

    private fun loadTableFieldI(decoder: InstructionDecoder) {
        this.getRegister(decoder.getB())
        this.pushInteger(decoder.getC() + 1)
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_GET_FIELD)
        this.setRegister(decoder.getA())
    }

    private fun storeTableFieldI(decoder: InstructionDecoder) {
        this.getRegister(decoder.getB())
        this.pushInteger(decoder.getC() + 1)
        this.getRegister(decoder.getA())
        this.callVirtual(TypeCache.VALUE.name, MethodCache.TABLE_SET_FIELD)
    }

    private fun loadTable(decoder: InstructionDecoder, position: Int) {
        val hashLog2 = decoder.getB() - 1
        val hashSize = if (hashLog2 == -1) 0 else 1 shl hashLog2
        val arraySize = this.instructList[position + 1]

        this.visitor.visitLdcInsn(hashSize + arraySize)
        this.callStatic(TypeCache.TABLE.name, MethodCache.TABLE_FROM)
        this.setRegister(decoder.getA())
    }

    private fun loadTableTemplate(decoder: InstructionDecoder) {
        this.pushConstant(decoder.getD())
        this.callStatic(TypeCache.TABLE.name, MethodCache.TABLE_COPY)
        this.setRegister(decoder.getA())
    }

    private fun setTableList(decoder: InstructionDecoder, position: Int) {
        val arrayIndex = this.instructList[position + 1]
        val start = decoder.getB()
        val last = start + decoder.getC()

        if (decoder.getC() == 0) {
            throw UnsupportedOperationException()
        }

        this.getRegister(decoder.getA())

        for (i in start until last) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.pushInteger(arrayIndex + i - start)
            this.getRegister(i)
            this.callVirtual(TypeCache.TABLE.name, MethodCache.TABLE_SET_FIELD)
        }

        this.visitor.visitInsn(Opcodes.POP)
    }

    private fun captureUpValue(decoder: InstructionDecoder) {
        val index = decoder.getB()

        when (decoder.getA()) {
            // Value capture
            0, 1 -> this.getRegister(index)
            // UpVal capture
            2, 3 -> this.pushSelfField("up$$index", TypeCache.VALUE.parameter)
        }
    }

    private fun captureUpValueList(decoder: InstructionDecoder, position: Int) {
        // We must reset the `decoder` after using it for look-ahead
        this.visitor.visitInsn(Opcodes.DUP)

        val index = decoder.getD()
        var offset = position + 1

        while (decoder.load(this.instructList, offset) && decoder.getOp() == Opcode.Capture) {
            this.captureUpValue(decoder)

            offset += 1
        }

        val paramList = TypeCache.VALUE.parameter.repeat(offset - position - 1)
        val info = MethodInfo("setUpValueList", "($paramList)V")

        decoder.load(this.instructList, position)
        this.callVirtual("luau/Func$$index", info)
    }

    private fun loadClosure(decoder: InstructionDecoder, position: Int) {
        val name = "luau/Func$" + decoder.getD()

        this.visitor.visitTypeInsn(Opcodes.NEW, name)
        this.visitor.visitInsn(Opcodes.DUP)
        this.visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)

        this.setRegister(decoder.getA())
        this.getRegister(decoder.getA())
        this.captureUpValueList(decoder, position)
    }

    private fun pushArrayRange(start: Int, last: Int) {
        this.visitor.visitLdcInsn(last - start + 1)
        this.visitor.visitTypeInsn(Opcodes.ANEWARRAY, TypeCache.VALUE.name)

        for (i in start..last) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.visitor.visitLdcInsn(i - start)
            this.getRegister(i)
            this.visitor.visitInsn(Opcodes.AASTORE)
        }
    }

    private fun popArrayRange(start: Int, last: Int) {
        for (i in start..last) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.visitor.visitLdcInsn(i - start)
            this.visitor.visitInsn(Opcodes.AALOAD)
            this.setRegister(i)
        }

        this.visitor.visitInsn(Opcodes.POP)
    }

    private fun doCall(decoder: InstructionDecoder) {
        val start = decoder.getA()
        val paramLast = start + decoder.getB() - 1
        val returnLast = start + decoder.getC() - 2

        if (decoder.getB() == 0 || decoder.getC() == 0) {
            throw UnsupportedOperationException()
        }

        this.getRegister(start)
        this.pushArrayRange(start + 1, paramLast)
        this.callVirtual(TypeCache.VALUE.name, MethodCache.CLOSURE_CALL)
        this.popArrayRange(start, returnLast)
    }

    private fun doReturn(decoder: InstructionDecoder) {
        val start = decoder.getA()
        val last = start + decoder.getB() - 2

        if (decoder.getB() == 0) {
            throw UnsupportedOperationException()
        }

        this.pushArrayRange(start, last)
        this.visitor.visitInsn(Opcodes.ARETURN)
    }

    private fun doUnOp(decoder: InstructionDecoder, method: MethodInfo) {
        this.getRegister(decoder.getB())
        this.callVirtual(TypeCache.VALUE.name, method)
        this.setRegister(decoder.getA())
    }

    private fun doBinOp(decoder: InstructionDecoder, method: MethodInfo) {
        this.getRegister(decoder.getB())
        this.getRegister(decoder.getC())
        this.callVirtual(TypeCache.VALUE.name, method)
        this.setRegister(decoder.getA())
    }

    private fun doBinOpConstant(decoder: InstructionDecoder, method: MethodInfo) {
        this.getRegister(decoder.getB())
        this.pushConstant(decoder.getC())
        this.callVirtual(TypeCache.VALUE.name, method)
        this.setRegister(decoder.getA())
    }

    private fun doConcatenation(decoder: InstructionDecoder) {
        this.pushArrayRange(decoder.getB(), decoder.getC())
        this.callStatic(TypeCache.AUXILIARY.name, MethodCache.VALUE_CONCAT)
        this.setRegister(decoder.getA())
    }

    // This translates a Luau instruction to a set of Java instructions.
    // There are notable differences in behavior here, as there are operations that we don't support.
    // We don't seek to benefit from fast-call mechanisms as-is and as such they behave as regular calls.
    // Up-values are also currently only on a per-closure copy basis.
    private fun addInstruction(decoder: InstructionDecoder, position: Int) {
        when (decoder.getOp()) {
            Opcode.Nop,
            Opcode.Break,
            Opcode.CloseUpValues,
            Opcode.PrepVariadic,
            Opcode.Coverage,
            Opcode.Capture -> this.visitor.visitInsn(Opcodes.NOP)

            Opcode.FastCall, Opcode.FastCall1, Opcode.FastCall2, Opcode.FastCall2K -> {
                // Fast calls are unsupported, but not necessary
                this.visitor.visitInsn(Opcodes.NOP)
            }

            Opcode.LoadNil -> this.loadNil(decoder.getA())
            Opcode.LoadBoolean -> this.loadBoolean(decoder, position)
            Opcode.LoadInteger -> {
                this.pushInteger(decoder.getD())
                this.setRegister(decoder.getA())
            }

            Opcode.LoadConstant -> {
                this.pushConstant(decoder.getD())
                this.setRegister(decoder.getA())
            }

            Opcode.Move -> {
                this.getRegister(decoder.getB())
                this.setRegister(decoder.getA())
            }

            Opcode.GetGlobal -> this.loadGlobal(decoder, position)
            Opcode.SetGlobal -> this.storeGlobal(decoder, position)
            Opcode.GetUpValue -> this.loadUpValue(decoder)
            Opcode.SetUpValue -> this.storeUpValue(decoder)
            Opcode.GetImport -> this.loadImport(decoder, position)
            Opcode.GetTable -> this.loadTableField(decoder)
            Opcode.SetTable -> this.storeTableField(decoder)
            Opcode.GetTableKey -> this.loadTableFieldK(decoder, position)
            Opcode.SetTableKey -> this.storeTableFieldK(decoder, position)
            Opcode.GetTableIndex -> this.loadTableFieldI(decoder)
            Opcode.SetTableIndex -> this.storeTableFieldI(decoder)
            Opcode.NewClosure -> this.loadClosure(decoder, position)

            Opcode.NameCall -> {
                this.loadTableFieldK(decoder, position)
                this.getRegister(decoder.getB())
                this.setRegister(decoder.getA() + 1)
            }

            Opcode.Call -> this.doCall(decoder)
            Opcode.Return -> this.doReturn(decoder)
            Opcode.Jump, Opcode.JumpSafe -> this.jumpAlways(position, decoder.getD())
            Opcode.JumpIfTruthy -> this.jumpIf(decoder, position, Opcodes.IFNE)
            Opcode.JumpIfFalsy -> this.jumpIf(decoder, position, Opcodes.IFEQ)
            Opcode.JumpIfEqual -> {
                this.getRegister(decoder.getA())
                this.getRegister(this.instructList[position + 1])
                this.jumpEqual(decoder, position, Opcodes.IFNE)
            }

            Opcode.JumpIfLessEqual -> this.jumpCompare(decoder, position, Opcodes.IFLE)
            Opcode.JumpIfLessThan -> this.jumpCompare(decoder, position, Opcodes.IFLT)
            Opcode.JumpIfNotEqual -> {
                this.getRegister(decoder.getA())
                this.getRegister(this.instructList[position + 1])
                this.jumpEqual(decoder, position, Opcodes.IFEQ)
            }

            Opcode.JumpIfMoreThan -> this.jumpCompare(decoder, position, Opcodes.IFGT)
            Opcode.JumpIfMoreEqual -> this.jumpCompare(decoder, position, Opcodes.IFGE)
            Opcode.Add -> this.doBinOp(decoder, MethodCache.VALUE_ADD)
            Opcode.Sub -> this.doBinOp(decoder, MethodCache.VALUE_SUB)
            Opcode.Mul -> this.doBinOp(decoder, MethodCache.VALUE_MUL)
            Opcode.Div -> this.doBinOp(decoder, MethodCache.VALUE_DIV)
            Opcode.Mod -> this.doBinOp(decoder, MethodCache.VALUE_MOD)
            Opcode.Pow -> this.doBinOp(decoder, MethodCache.VALUE_POW)
            Opcode.AddConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_ADD)
            Opcode.SubConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_SUB)
            Opcode.MulConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_MUL)
            Opcode.DivConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_DIV)
            Opcode.ModConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_MOD)
            Opcode.PowConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_POW)
            Opcode.And -> this.doBinOp(decoder, MethodCache.VALUE_AND)
            Opcode.Or -> this.doBinOp(decoder, MethodCache.VALUE_OR)
            Opcode.AndConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_AND)
            Opcode.OrConstant -> this.doBinOpConstant(decoder, MethodCache.VALUE_OR)
            Opcode.Concat -> this.doConcatenation(decoder)
            Opcode.Not -> this.doUnOp(decoder, MethodCache.VALUE_NOT)
            Opcode.Minus -> this.doUnOp(decoder, MethodCache.VALUE_MINUS)
            Opcode.Length -> this.doUnOp(decoder, MethodCache.VALUE_LENGTH)
            Opcode.NewTable -> this.loadTable(decoder, position)
            Opcode.DupTable -> this.loadTableTemplate(decoder)
            Opcode.SetList -> this.setTableList(decoder, position)
            Opcode.ForNumericPrep -> {
                val start = decoder.getA()
                val label = this.findLabelContaining(position + decoder.getD() + 1)

                this.getRegister(start)
                this.getRegister(start + 1)
                this.getRegister(start + 2)
                this.callStatic(TypeCache.AUXILIARY.name, MethodCache.FOR_NUMERIC_TEST)
                this.visitor.visitJumpInsn(Opcodes.IFEQ, label)
            }

            Opcode.ForNumericLoop -> {
                val start = decoder.getA()

                this.getRegister(start + 2)
                this.getRegister(start + 1)
                this.callVirtual(TypeCache.NUMBER.name, MethodCache.VALUE_ADD)
                this.setRegister(start + 2)
                this.jumpAlways(position, decoder.getD() - 1)
            }
//            Opcode.ForGenericLoop -> TODO()
//            Opcode.ForGenericPrepINext -> TODO()
//            Opcode.ForGenericLoopINext -> TODO()
//            Opcode.ForGenericPrepNext -> TODO()
//            Opcode.ForGenericLoopNext -> TODO()
//            Opcode.GetVariadic -> TODO()
            Opcode.DupClosure -> {
                this.pushConstant(decoder.getD())
                this.setRegister(decoder.getA())
            }

            Opcode.LoadConstantEx -> {
                this.pushConstant(this.instructList[position + 1])
                this.setRegister(decoder.getA())
            }

            Opcode.JumpEx -> this.jumpAlways(position, decoder.getE())
            Opcode.JumpIfConstant -> {
                this.getRegister(decoder.getA())
                this.pushConstant(this.instructList[position + 1])
                this.jumpEqual(decoder, position, Opcodes.IFNE)
            }

            Opcode.JumpIfNotConstant -> {
                this.getRegister(decoder.getA())
                this.pushConstant(this.instructList[position + 1])
                this.jumpEqual(decoder, position, Opcodes.IFEQ)
            }
//            Opcode.ForGenericPrep -> TODO()
            else -> {}
        }
    }

    private fun addCodeBlock(block: CodeBlock, decoder: InstructionDecoder) {
        this.visitor.visitLabel(block.label)

        var index = 0

        while (decoder.load(block.instructList, index)) {
            this.addInstruction(decoder, block.start + index)

            index += decoder.getOp().length()
        }
    }

    private fun addLocalInfo() {
        for (local in this.debugInfo.localList) {
            val start = this.findLabelContaining(local.startPc)
            val end = this.findLabelContaining(local.endPc)
            val name = this.resolver.getString(local.name)

            this.visitor.visitLocalVariable(name, TypeCache.VALUE.parameter, null, start, end, local.register + 1)
        }
    }

    private fun loadParameters() {
        if (this.metaData.numParameter == 0) {
            return
        }

        this.getRegister(0)

        for (i in 0 until this.metaData.numParameter) {
            this.visitor.visitInsn(Opcodes.DUP)
            this.visitor.visitLdcInsn(i)
            this.visitor.visitInsn(Opcodes.AALOAD)
            this.setRegister(i)
        }

        this.visitor.visitInsn(Opcodes.POP)
    }

    override fun apply(
        visitor: MethodVisitor,
        context: Implementation.Context,
        instrumented: MethodDescription
    ): ByteCodeAppender.Size {
        this.owner = context.instrumentedType.internalName
        this.visitor = visitor
        this.blockList = BlockBuilder.getBlockList(this.instructList, this.debugInfo.localList)

        val decoder = InstructionDecoder()

        this.loadParameters()
        this.blockList.forEach { this.addCodeBlock(it, decoder) }
        this.addLocalInfo()

        return ByteCodeAppender.Size(10, this.metaData.maxStackSize + 1)
    }
}
