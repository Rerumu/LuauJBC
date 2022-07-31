package codegen.appender

import net.bytebuddy.asm.AsmVisitorWrapper
import net.bytebuddy.description.field.FieldDescription
import net.bytebuddy.description.field.FieldList
import net.bytebuddy.description.method.MethodList
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.jar.asm.ClassVisitor
import net.bytebuddy.jar.asm.ClassWriter
import net.bytebuddy.pool.TypePool

class FrameComputing : AsmVisitorWrapper {
    override fun mergeWriter(flags: Int): Int {
        return flags or ClassWriter.COMPUTE_FRAMES
    }

    override fun mergeReader(flags: Int): Int {
        return flags or ClassWriter.COMPUTE_FRAMES
    }

    override fun wrap(
        instrumentedType: TypeDescription,
        classVisitor: ClassVisitor,
        implementationContext: Implementation.Context,
        typePool: TypePool,
        fields: FieldList<FieldDescription.InDefinedShape>,
        methods: MethodList<*>,
        writerFlags: Int,
        readerFlags: Int
    ): ClassVisitor {
        return classVisitor
    }
}
