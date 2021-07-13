enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "Result"

includeBuild("plugins")

include(":result")

pluginManagement {
    val kotlinVersion = rootDir.resolve("gradle/libs.versions.toml").reader().use { java.util.Properties().apply { load(it) } }
        .getProperty("kotlin")
        .removeSurrounding("\"")

    repositories {}
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}
