import java.io.IOException

plugins {
    kotlin("multiplatform")

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
    js(IR) {
        browser()
        binaries.executable()
    }
    iosSimulatorArm64()
    macosArm64()
    macosX64()

    // Apply the default hierarchy again. It'll create, for example, the iosMain source set:
    applyDefaultHierarchyTemplate()

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting

        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.test)
            }
        }

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test.js)
            }
        }
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
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

    val copyTestResourceJs by registering(Copy::class) {
        from("$projectDir/src/commonTest/resources")
        into("${rootProject.buildDir}/js/packages/${rootProject.name}-${project.name}-test/src/commonTest/resources")
    }

    val jsTest by getting {
        dependsOn(copyTestResourceJs)
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
