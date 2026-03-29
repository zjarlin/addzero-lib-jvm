import org.gradle.api.tasks.WriteProperties

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `java-gradle-plugin`
}

val libs = versionCatalogs.named("libs")

group = "site.addzero"

dependencies {
    implementation(gradleApi())
    implementation(project(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base"))
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

gradlePlugin {
    plugins {
        create("logger") {
            id = "site.addzero.ksp.logger"
            implementationClass = "site.addzero.ksp.logger.gradle.LoggerGradlePlugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/logger/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateLoggerPluginCoordinates = tasks.register<WriteProperties>("generateLoggerPluginCoordinates") {
    destinationFile = generatedCoordinatesDir
        .map { dir -> dir.file("site/addzero/ksp/logger/gradle-plugin.properties") }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateLoggerPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
