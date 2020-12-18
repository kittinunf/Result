sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/main/kotlin")
}

dependencies {
    val junit: String by project
    val kotlinVersion: String by project
    val coroutines: String by project

    implementation(kotlin("stdlib", kotlinVersion))

    testImplementation("junit:junit:$junit")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
}
