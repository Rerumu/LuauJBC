package codegen

import codegen.typeinfo.MethodInfo
import codegen.typeinfo.TypeCache
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.FieldManifestation
import net.bytebuddy.description.modifier.ModifierContributor
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.bytecode.ByteCodeAppender
import net.bytebuddy.jar.asm.Opcodes
import types.StringType
import java.nio.charset.StandardCharsets

private val PUB_FS_MODIFIER =
    ModifierContributor.Resolver.of(Visibility.PUBLIC, FieldManifestation.FINAL, Ownership.STATIC).resolve()

class StringCacheBuilder(private val stringList: List<ByteArray>) {
    private var builder = ByteBuddy().subclass(Object::class.java).name("luau.StringCache")

    fun getReady(): DynamicType.Unloaded<Object> {
        this.addStringList()
        this.addInitializers()

        return builder.make()
    }

    private fun addStringList() {
        for (i in 0 until this.stringList.size) {
            this.builder = this.builder.defineField("data$$i", StringType::class.java, PUB_FS_MODIFIER)
        }
    }

    private fun addInitializers() {
        this.builder = this.builder.initializer { visitor, _, _ ->
            val ownerName = this.builder.toTypeDescription().internalName

            this.stringList.forEachIndexed { i, v ->
                val encoded = String(v, StandardCharsets.ISO_8859_1)

                visitor.visitTypeInsn(Opcodes.NEW, TypeCache.STRING.name)
                visitor.visitInsn(Opcodes.DUP)
                visitor.visitLdcInsn(encoded)
                visitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    TypeCache.STRING.name,
                    "<init>",
                    MethodInfo.STRING_CONSTRUCTOR,
                    false
                )
                visitor.visitFieldInsn(Opcodes.PUTSTATIC, ownerName, "data$$i", TypeCache.STRING.parameter)
            }

            ByteCodeAppender.Size(3, 0)
        }
    }
}
