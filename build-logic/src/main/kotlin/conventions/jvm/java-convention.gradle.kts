plugins {
    id("j8support")
    id("utf8support")
    `java-library`
}

extensions.configure<JavaPluginExtension> {
    withSourcesJar()
}
tasks.test {
    useJUnitPlatform()
}

