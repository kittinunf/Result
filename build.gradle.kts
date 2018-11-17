import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", extra.get("kotlin") as String))
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${extra.get("bintray")}")
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    apply {
        plugin<JavaLibraryPlugin>()
        plugin<KotlinPlatformJvmPlugin>()
        plugin("maven-publish")
        plugin("com.jfrog.bintray")
        plugin("jacoco")
    }

    configure<JacocoPluginExtension> {
        toolVersion = extra.get("jacoco") as String
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }
}
