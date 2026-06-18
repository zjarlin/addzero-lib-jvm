import org.gradle.api.tasks.WriteProperties
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `kotlin-dsl`
    `java-gradle-plugin`
}

val libs = versionCatalogs.named("libs")

group = "site.addzero"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.findLibrary("gradle-ksp-consumer-base").get())
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/gen-reified/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateGenReifiedPluginCoordinates = tasks.register<WriteProperties>("generateGenReifiedPluginCoordinates") {
    destinationFile = generatedCoordinatesDir
        .map { dir -> dir.file("site/addzero/ksp/gen-reified/gradle-plugin.properties") }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateGenReifiedPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
}
