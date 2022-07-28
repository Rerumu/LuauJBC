package codegen

import bytecode.Module
import net.bytebuddy.dynamic.DynamicType
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
}
