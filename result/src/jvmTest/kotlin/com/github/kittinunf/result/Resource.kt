package com.github.kittinunf.result

import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Resource actual constructor(actual val name: String) {
    private val file = File("$RESOURCE_DIR/$name")

    actual fun exists(): Boolean = file.exists()

    actual fun read(): String = file.readText()
}
