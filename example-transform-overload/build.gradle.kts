import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

group = "site.addzero.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib:kcp:transform-overload:kcp-transform-overload-annotations"))
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(":lib:kcp:transform-overload:kcp-transform-overload-plugin:jar")

    val pluginProject = project(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
    val pluginVersion = pluginProject.version
    val pluginJar = pluginProject.layout.buildDirectory.file("libs/kcp-transform-overload-plugin-${pluginVersion}.jar")

    doFirst {
        val jarFile = pluginJar.get().asFile
        if (!jarFile.exists()) {
            throw GradleException("Plugin jar not found: ${jarFile.absolutePath}")
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            provider {
                val jarFile = pluginJar.get().asFile
                listOf("-Xplugin=${jarFile.absolutePath}")
            }
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("site.addzero.example.GeneratedMethodsDemoKt")
}
