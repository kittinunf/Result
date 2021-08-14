package com.github.kittinunf.result

import java.io.File

actual class Resource actual constructor(actual val name: String) {
    private val file = File("$resource/$name")

    actual fun exists(): Boolean = file.exists()

    actual fun read(): String = file.readText()
}

actual fun readFile(directory: String, fileName: String): String =
    File(System.getProperty("user.dir")).resolve(directory).resolve(fileName).readText()
