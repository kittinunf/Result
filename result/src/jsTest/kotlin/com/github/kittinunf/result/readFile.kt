package com.github.kittinunf.result

external fun require(name: String): dynamic
external val __dirname: dynamic

val fs = require("fs")
val path = require("path");

actual fun readFile(directory: String, fileName: String): String {
    val path = path.join(
        __dirname,
        "..\\..\\..\\..\\..\\..",
        "commonTest",
        "resources",
        fileName
    )
    return fs.readFileSync(path, "utf8") as String)
}
