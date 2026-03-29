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
        create("jdbc2entity") {
            id = "site.addzero.ksp.jdbc2entity"
            implementationClass = "site.addzero.ksp.jdbc2entity.gradle.Jdbc2EntityGradlePlugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/jdbc2entity/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateJdbc2EntityPluginCoordinates = tasks.register<WriteProperties>("generateJdbc2EntityPluginCoordinates") {
    destinationFile = generatedCoordinatesDir
        .map { dir -> dir.file("site/addzero/ksp/jdbc2entity/gradle-plugin.properties") }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateJdbc2EntityPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
