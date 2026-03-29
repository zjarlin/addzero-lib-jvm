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
        create("controller2iso2dataprovider") {
            id = "site.addzero.ksp.controller2iso2dataprovider"
            implementationClass =
                "site.addzero.ksp.controller2iso2dataprovider.gradle.Controller2Iso2DataProviderGradlePlugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/controller2iso2dataprovider/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateController2Iso2DataProviderPluginCoordinates =
    tasks.register<WriteProperties>("generateController2Iso2DataProviderPluginCoordinates") {
        destinationFile = generatedCoordinatesDir
            .map { dir -> dir.file("site/addzero/ksp/controller2iso2dataprovider/gradle-plugin.properties") }
            .get()
            .asFile
        encoding = "UTF-8"
        property("groupId", groupId)
        property("version", pluginVersion)
    }

tasks.processResources {
    dependsOn(generateController2Iso2DataProviderPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
