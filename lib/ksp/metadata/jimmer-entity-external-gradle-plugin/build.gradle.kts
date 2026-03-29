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
        create("jimmerEntityExternal") {
            id = "site.addzero.ksp.jimmer-entity-external"
            implementationClass =
                "site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalGradlePlugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/jimmer-entity-external/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateJimmerEntityExternalPluginCoordinates =
    tasks.register<WriteProperties>("generateJimmerEntityExternalPluginCoordinates") {
        destinationFile = generatedCoordinatesDir
            .map { dir -> dir.file("site/addzero/ksp/jimmer-entity-external/gradle-plugin.properties") }
            .get()
            .asFile
        encoding = "UTF-8"
        property("groupId", groupId)
        property("version", pluginVersion)
    }

tasks.processResources {
    dependsOn(generateJimmerEntityExternalPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
