package com.github.kittinunf.result

import java.io.File

actual fun readFile(directory: String, fileName: String): String =
    File(System.getProperty("user.dir")).resolve(directory).resolve(fileName).readText()
