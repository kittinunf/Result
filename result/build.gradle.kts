import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import java.io.IOException

plugins {
    kotlin("multiplatform") version "1.9.21"

    java
    jacoco

    id("publication")
}

val artifactGroupId: String by project
group = artifactGroupId

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

val artifactPublishVersion: String by project
version = if (isReleaseBuild) artifactPublishVersion else "master-$gitSha-SNAPSHOT"

kotlin {
    jvm()
    iosX64()
    iosArm64()
    watchosArm64()
    watchosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.executable()
    }

    iosSimulatorArm64()
    macosArm64()
    macosX64()
    watchosX64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks {
    withType<JacocoReport> {
        group = "Reporting"
        description = "Generate Jacoco coverage reports."

        val jvmTest by getting
        dependsOn(jvmTest)

        val classFiles = File("$buildDir/classes/kotlin/jvm/main").walkBottomUp().toSet()
        classDirectories.setFrom(classFiles)
        sourceDirectories.setFrom(files(arrayOf("$projectDir/src/commonMain")))
        executionData.setFrom(files("$buildDir/jacoco/jvmTest.exec"))

        reports {
            xml.required.set(true)

            html.required.set(true)
            html.outputLocation.set(buildDir.resolve("reports"))

            csv.required.set(false)
        }
    }
}

fun String.runCommand(workingDir: File): String? =
    try {
        val parts = split("\\s".toRegex())
        val proc =
            ProcessBuilder(*parts.toTypedArray())
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
