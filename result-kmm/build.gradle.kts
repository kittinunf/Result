import java.io.IOException

plugins {
    kotlin("multiplatform")
//    id("publication")
}

val artifactGroupId: String by project
group = artifactGroupId

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()
val artifactPublishVersion: String by project

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

version = if (isReleaseBuild) artifactPublishVersion else "master-$gitSha-SNAPSHOT"
println(version)

kotlin {
    jvm()

    sourceSets {
        all {}

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.test)
            }
        }

        val jvmMain by getting {}

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}

fun String.runCommand(workingDir: File): String? = try {
    val parts = split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(30, TimeUnit.SECONDS)
    proc.inputStream.bufferedReader().readText()
} catch (e: IOException) {
    e.printStackTrace()
    null
}
