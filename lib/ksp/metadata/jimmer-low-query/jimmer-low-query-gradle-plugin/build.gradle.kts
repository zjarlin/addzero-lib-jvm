import org.gradle.api.tasks.WriteProperties
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    `kotlin-dsl`
    `java-gradle-plugin`
}

version = "2026.06.26"

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
    implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-gradle-plugin").get())
    implementation(libs.findLibrary("com-google-devtools-ksp-com-google-devtools-ksp-gradle-plugin").get())
    testImplementation(gradleTestKit())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

sourceSets {
    main {
        kotlin.srcDir("generated/main/kotlin/site/addzero/ksp/generated")
    }
}

val generatedCoordinatesDir = layout.buildDirectory.dir("generated/jimmer-low-query/resources")
val groupId = project.group.toString()
val pluginVersion = project.version.toString()

val generateJimmerLowQueryPluginCoordinates = tasks.register<WriteProperties>("generateJimmerLowQueryPluginCoordinates") {
    destinationFile = generatedCoordinatesDir
        .map { dir -> dir.file("site/addzero/ksp/jimmer-low-query/gradle-plugin.properties") }
        .get()
        .asFile
    encoding = "UTF-8"
    property("groupId", groupId)
    property("version", pluginVersion)
}

tasks.processResources {
    dependsOn(generateJimmerLowQueryPluginCoordinates)
    from(generatedCoordinatesDir)
}

tasks.test {
    useJUnitPlatform()
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}
