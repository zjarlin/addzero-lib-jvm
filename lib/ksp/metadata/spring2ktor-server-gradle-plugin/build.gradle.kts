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

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/spring2ktor-server/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateSpring2KtorServerPluginCoordinates =
    tasks.register<WriteProperties>("generateSpring2KtorServerPluginCoordinates") {
        destinationFile = generatedCoordinatesDir
            .map { dir -> dir.file("site/addzero/ksp/spring2ktor-server/gradle-plugin.properties") }
            .get()
            .asFile
        encoding = "UTF-8"
        property("groupId", groupId)
        property("version", pluginVersion)
    }

tasks.processResources {
    dependsOn(generateSpring2KtorServerPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
