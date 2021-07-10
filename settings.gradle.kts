enableFeaturePreview("VERSION_CATALOGS")

//include(":result", ":result-coroutines")

include(":result-kmm")

pluginManagement {
    repositories {
    }
    plugins {
        kotlin("multiplatform") version "1.5.20"
    }
}

dependencyResolutionManagement {
    defaultLibrariesExtensionName.set("libs")
}
