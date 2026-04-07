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
    implementation(project(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base"))
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/modbus-rtu/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateModbusRtuPluginCoordinates = tasks.register<WriteProperties>("generateModbusRtuPluginCoordinates") {
    destinationFile = generatedCoordinatesDir
        .map { dir -> dir.file("site/addzero/ksp/modbus-rtu/gradle-plugin.properties") }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateModbusRtuPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
