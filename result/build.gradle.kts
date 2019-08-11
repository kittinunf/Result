sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/main/kotlin")
}

dependencies {
    val junit: String by project

    implementation(kotlin("stdlib"))

    testImplementation("junit:junit:$junit")
}
