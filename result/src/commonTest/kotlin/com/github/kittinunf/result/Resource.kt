package com.github.kittinunf.result

const val resource = "./src/commonTest/resources"

expect class Resource(name: String) {
    val name: String

    fun exists(): Boolean
    fun read(): String
}