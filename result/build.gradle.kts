sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/main/kotlin")
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("junit:junit:${extra.get("junit")}")
}
