import bytecode.Module
import bytecode.ModuleReader
import codegen.ObjectBuilder
import net.bytebuddy.ByteBuddy
import template.Auxiliary
import types.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.jar.Attributes
import java.util.jar.Manifest

fun getManifest(): Manifest {
    val manifest = Manifest()
    val attr = manifest.mainAttributes

    attr[Attributes.Name.MANIFEST_VERSION] = "1.0"
    attr[Attributes.Name.MAIN_CLASS] = "template.Program"

    return manifest
}

fun getModuleOf(name: String): Module {
    val data = File(name).readBytes()
    val buffer = ByteBuffer.wrap(data)

    buffer.order(ByteOrder.LITTLE_ENDIAN)

    return ModuleReader.readModule(buffer)
}

fun addLoadedClass(file: File, klass: Class<out Any>) {
    ByteBuddy().redefine(klass).make().inject(file)
}

fun addBuiltIn(file: File) {
    addLoadedClass(file, ValueType::class.java)
    addLoadedClass(file, NilType::class.java)
    addLoadedClass(file, BooleanType::class.java)
    addLoadedClass(file, NumberType::class.java)
    addLoadedClass(file, StringType::class.java)
    addLoadedClass(file, TableType::class.java)
    addLoadedClass(file, ClosureType::class.java)

    addLoadedClass(file, Auxiliary::class.java)
    addLoadedClass(file, BuiltIn::class.java)

    for (tuple in BuiltIn.INSTANCE.map) {
        addLoadedClass(file, tuple.value.javaClass)
    }
}

fun main(file_list: Array<String>) {
    if (file_list.size != 1) {
        println("Must provide exactly one file!")
        return
    }

    val builder = ObjectBuilder(getModuleOf(file_list[0]))
    val file = File("working_at/Luau.jar")

    builder.genEntryPoint().toJar(file, getManifest())

    builder.genStringList().inject(file)
    builder.genClosureList().forEach { it.inject(file) }

    addBuiltIn(file)
}
