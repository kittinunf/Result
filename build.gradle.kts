import com.jfrog.bintray.gradle.BintrayExtension
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

    // publishing
    val sourceSets = project.the<SourceSetContainer>()

    val sourcesJar by tasks.registering(Jar::class) {
        from(sourceSets["main"].allSource)
        classifier = "sources"
    }

    val doc by tasks.creating(Javadoc::class) {
        isFailOnError = false
        source = sourceSets["main"].allJava
    }
    val javadocJar by tasks.creating(Jar::class) {
        dependsOn(doc)
        from(doc)

        classifier = "javadoc"
    }

    val artifactPublish: String by extra
    val artifactGroupId: String by extra

    version = artifactPublish
    group = artifactGroupId

    configure<PublishingExtension> {
        publications {
            register(project.name, MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar)
                groupId = artifactGroupId
                artifactId = project.name
                version = artifactPublish
            }
        }
    }

    // bintray
    configure<BintrayExtension> {
        user = findProperty("BINTRAY_USER") as? String
        key = findProperty("BINTRAY_KEY") as? String
        setPublications(project.name)
        pkg.apply {
            repo = "maven"
            name = "Result"
            desc = "The modelling for success/failure of operations in Kotlin"
            userOrg = "kittinunf"
            websiteUrl = "https://github.com/kittinunf/Result"
            vcsUrl = "https://github.com/kittinunf/Result"
            setLicenses("MIT")
            version.apply {
                name = artifactPublish
            }
        }
    }

    // jacoco
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
