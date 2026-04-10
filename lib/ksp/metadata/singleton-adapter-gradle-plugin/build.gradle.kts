import org.gradle.api.tasks.WriteProperties

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `kotlin-dsl`
    `java-gradle-plugin`
}

val libs = versionCatalogs.named("libs")

group = "site.addzero"

dependencies {
    implementation(gradleApi())
    implementation(libs.findLibrary("gradle-ksp-consumer-base").get())
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/singleton-adapter/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateSingletonAdapterPluginCoordinates =
    tasks.register<WriteProperties>("generateSingletonAdapterPluginCoordinates") {
        destinationFile = generatedCoordinatesDir
            .map { dir -> dir.file("site/addzero/ksp/singleton-adapter/gradle-plugin.properties") }
            .get()
            .asFile
        encoding = "UTF-8"
        property("groupId", groupId)
        property("version", pluginVersion)
    }

tasks.processResources {
    dependsOn(generateSingletonAdapterPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
