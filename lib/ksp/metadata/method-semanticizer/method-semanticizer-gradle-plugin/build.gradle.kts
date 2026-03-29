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
        create("methodSemanticizer") {
            id = "site.addzero.ksp.method-semanticizer"
            implementationClass =
                "site.addzero.ksp.methodsemanticizer.gradle.MethodSemanticizerGradlePlugin"
        }
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/method-semanticizer/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateMethodSemanticizerPluginCoordinates =
    tasks.register<WriteProperties>("generateMethodSemanticizerPluginCoordinates") {
        destinationFile = generatedCoordinatesDir
            .map { dir -> dir.file("site/addzero/ksp/method-semanticizer/gradle-plugin.properties") }
            .get()
            .asFile
        encoding = "UTF-8"
        property("groupId", groupId)
        property("version", pluginVersion)
    }

tasks.processResources {
    dependsOn(generateMethodSemanticizerPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
