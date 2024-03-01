package com.github.kittinunf.result

const val RESOURCE_DIR = "./src/commonTest/resources"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Resource(name: String) {
    val name: String

    fun exists(): Boolean

    fun read(): String
}
