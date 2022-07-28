package codegen

import types.StringType

class StringResolver(private val stringList: List<ByteArray>) {
    fun getString(index: Int): String? {
        return if (index == 0) null else StringType(this.stringList[index - 1]).toString()
    }
}
