import bytecode.Module
import bytecode.ModuleReader
import codegen.ObjectBuilder
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun getModuleOf(name: String): Module {
    val data = File(name).readBytes()
    val buffer = ByteBuffer.wrap(data)

    buffer.order(ByteOrder.LITTLE_ENDIAN)

    return ModuleReader.readModule(buffer)
}

fun main(file_list: Array<String>) {
    if (file_list.size != 1) {
        println("Must provide exactly one file!")
        return
    }

    val builder = ObjectBuilder(getModuleOf(file_list[0]))
    val saving = File("working_at")

    builder.genStringList().saveIn(saving)

    for (ready in builder.genClosureList()) {
        ready.saveIn(saving)
    }
}
