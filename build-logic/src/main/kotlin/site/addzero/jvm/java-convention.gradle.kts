
package site.addzero.jvm
plugins {
    id("site.addzero.jvm.j8support")
    id("site.addzero.jvm.utf8support")
    `java-library`
}

extensions.configure<JavaPluginExtension> {
    withSourcesJar()
}
tasks.test {
    useJUnitPlatform()
}

