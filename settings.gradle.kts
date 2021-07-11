enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "Result"

include(":result-kmm")

pluginManagement {
    val kotlinVersion = rootDir.resolve("gradle/libs.versions.toml").readLines()
        .first { it.contains("kotlin") }
        .substringAfter("=")
        .removeSurrounding("\"")

    repositories {}
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}
