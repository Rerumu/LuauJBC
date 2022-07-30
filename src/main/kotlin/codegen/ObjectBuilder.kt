package codegen

import bytecode.Module
import codegen.typeinfo.TypeCache
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.Opcodes
import template.Program
import types.ClosureType

class ObjectBuilder(private val module: Module) {
    fun genStringList(): DynamicType.Unloaded<Object> {
        return StringCacheBuilder(this.module.stringList).getReady()
    }

    fun genClosureList(): List<DynamicType.Unloaded<ClosureType>> {
        val resolver = StringResolver(module.stringList)

        return this.module.functionList.mapIndexed { index, function ->
            val builder = FunctionBuilder(resolver, function)

            builder.getReady(index)
        }
    }

    fun genEntryPoint(): DynamicType.Unloaded<Program> {
        val name = "luau/Func$" + module.entryPoint
        val data = ByteBuddy().redefine(Program::class.java)
            .initializer { visitor, _, _ ->
                val programName = TypeCache.PROGRAM.name
                val closureType = TypeCache.CLOSURE.parameter

                visitor.visitTypeInsn(Opcodes.NEW, name)
                visitor.visitInsn(Opcodes.DUP)
                visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
                visitor.visitFieldInsn(Opcodes.PUTSTATIC, programName, "entryPoint", closureType)

                ByteCodeAppender.Size(2, 0)
            }

        return data.make()
    }
}
