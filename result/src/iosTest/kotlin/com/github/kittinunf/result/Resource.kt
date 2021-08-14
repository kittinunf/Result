package com.github.kittinunf.result

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import platform.posix.SEEK_END
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind

actual class Resource actual constructor(actual val name: String) {
    private val file = fopen("$resource/$name", "r")

    actual fun exists(): Boolean = file != null

    actual fun read(): String {
        fseek(file, 0, SEEK_END)
        val size = ftell(file)
        rewind(file)

        return memScoped {
            val tmp = allocArray<ByteVar>(size)
            val ret = fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
            tmp.toKString()
        }
    }
}
