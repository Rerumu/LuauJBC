package codegen

import java.nio.charset.StandardCharsets

class StringResolver(private val stringList: List<ByteArray>) {
    fun getString(index: Int): String? {
        return if (index == 0)
            null
        else
            String(this.stringList[index - 1], StandardCharsets.ISO_8859_1)
    }
}
