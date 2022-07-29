package codegen

import bytecode.Constant
import bytecode.Function
import codegen.appender.CodeAppender
import codegen.appender.DataAppender
import codegen.typeinfo.TypeCache
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.FieldManifestation
import net.bytebuddy.description.modifier.ModifierContributor
import net.bytebuddy.description.modifier.TypeManifestation
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.matcher.ElementMatchers
import reflection.Reflection
import types.ClosureType
import types.TableType
import types.ValueType

private val CLASS_MODIFIER = ModifierContributor.Resolver.of(Visibility.PUBLIC, TypeManifestation.FINAL).resolve()
private val PRV_F_MODIFIER = ModifierContributor.Resolver.of(Visibility.PRIVATE, FieldManifestation.FINAL).resolve()
private val PRV_MODIFIER = Visibility.PRIVATE.mask

class FunctionBuilder(private val resolver: StringResolver, private val function: Function) {
    private var builder = ByteBuddy().subclass(ClosureType::class.java)

    fun getReady(index: Int): DynamicType.Unloaded<ClosureType> {
        this.builder = this.builder.name("luau.Func$$index").modifiers(CLASS_MODIFIER)

        this.addReflection()
        this.addConstantList()
        this.addEnvironment()
        this.addUpValueList()
        this.addCode()

        return this.builder.make()
    }

    private fun getParametersListed(): String {
        return (0 until this.function.metaData.numParameter).joinToString(", ") {
            val localData = this.function.debugInfo.localList.find { loc -> loc.startPc == 0 && loc.register == it }
            val name = this.resolver.getString(localData?.name ?: 0)

            name ?: "param$$it"
        }
    }

    private fun getVarargEllipses(): String {
        return if (this.function.metaData.isVararg)
            if (this.function.metaData.numParameter == 0) "..." else ", ..."
        else
            ""
    }

    private fun addReflection() {
        val name = this.resolver.getString(this.function.debugInfo.name) ?: "unnamed"
        val parameters = this.getParametersListed()
        val ellipses = this.getVarargEllipses()
        val upValues = this.getUpValuesListed()

        this.builder = this.builder.annotateType(
            Reflection(signature = "$name($parameters$ellipses)", upValues = upValues)
        )
    }

    private fun addConstant(index: Int, constant: Constant) {
        val typeClass = TypeCache.fromConstant(constant).klass

        this.builder = this.builder.defineField("data$$index", typeClass, PRV_F_MODIFIER)
    }

    private fun addConstantList() {
        val appender = DataAppender(this.function.constantList)

        this.function.constantList.forEachIndexed { i, c -> this.addConstant(i, c) }
        this.builder = this.builder.initializer(appender)
    }

    private fun addEnvironment() {
        this.builder = this.builder.defineField("environment", TableType::class.java, PRV_MODIFIER)
    }

    private fun getUpValuesListed(): String {
        return (0 until this.function.metaData.numUpValue).joinToString(", ") {
            val index = this.function.debugInfo.upValueList.getOrElse(it) { 0 }
            val name = this.resolver.getString(index)

            name ?: "upValue$$it"
        }
    }

    private fun addUpValue(index: Int) {
        this.builder = this.builder.defineField("up$$index", ValueType::class.java, PRV_MODIFIER)
    }

    private fun addUpValueList() {
        (0 until this.function.metaData.numUpValue).forEach { this.addUpValue(it) }
    }

    private fun addCode() {
        val appender = CodeAppender(this.resolver, this.function)

        this.builder = this.builder.method(ElementMatchers.named("call"))
            .intercept(Implementation.Simple(appender))
    }
}
