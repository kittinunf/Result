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
            fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
            tmp.toKString()
        }
    }
}

actual fun readFile(directory: String, fileName: String): String {
    return when {
        fileName.contains("lorem_short") -> {
            """
Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
      """.trimIndent()
        }
        fileName.contains("lorem_long") -> {
            """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus mollis eleifend libero, id elementum sem sagittis sit amet.
Donec lorem nulla, mollis eu diam eget, varius facilisis ipsum. Duis turpis lorem, iaculis quis venenatis nec, placerat eget est.
Nullam posuere leo sit amet diam laoreet finibus. Suspendisse at tempor nibh, sit amet maximus elit.
Duis est sem, cursus commodo dapibus ultricies, vestibulum in eros. Suspendisse feugiat vel nibh in viverra. Vivamus posuere sodales sapien.
Donec ex purus, fringilla in lacinia nec, porttitor vitae ante. Suspendisse elementum iaculis nisl, eget aliquam ligula iaculis in.
Maecenas vestibulum diam in nisi faucibus, vel fringilla tortor facilisis. Nulla vitae tortor volutpat, facilisis nulla id, convallis nulla.
Integer at orci eleifend, tincidunt ligula in, sagittis sapien. Aliquam molestie arcu venenatis leo congue, quis elementum felis pellentesque.
Vivamus augue dui, vestibulum quis justo a, rhoncus vulputate dolor. Pellentesque ullamcorper ligula lorem, eget tempor neque viverra vel.
Vivamus faucibus id lacus sed aliquet. Curabitur faucibus tristique ante eu dictum. Fusce ipsum ipsum, vulputate ac lorem condimentum, sollicitudin feugiat dolor.
Aliquam erat volutpat. Ut suscipit fringilla rutrum. Vivamus nec posuere libero, a accumsan lectus.
Aenean quis elit sollicitudin, congue nulla posuere, imperdiet odio. Aenean sit amet ex imperdiet, ultrices enim et, varius erat.
Donec et ultricies ante. Aenean sagittis, purus euismod bibendum iaculis, augue tellus dignissim odio, vitae consequat leo arcu non dui.
Vestibulum commodo nibh ex, at porttitor mi tristique in. Donec bibendum, sapien bibendum aliquam pharetra, lorem magna scelerisque tellus, eget vestibulum eros lorem nec urna.
Proin elementum libero in viverra dignissim. Morbi commodo eget tellus a congue. Suspendisse potenti. Interdum et malesuada fames ac ante ipsum primis in faucibus.
Praesent euismod aliquet ligula, et consequat tortor sodales feugiat. Praesent et dolor felis.
        """.trimIndent()
        }
        else -> throw RuntimeException("src/commonTest/resources/$fileName (No such file or directory)")
    }
}
