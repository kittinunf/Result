sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/main/kotlin")
}

dependencies {
    val junit: String by project
    val coroutines: String by project
    val kotlinVersion: String by project

    implementation(project(":result"))

    implementation(kotlin("stdlib", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")

    testImplementation("junit:junit:$junit")
}
