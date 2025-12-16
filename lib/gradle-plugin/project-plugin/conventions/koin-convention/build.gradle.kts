plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    gradleApi()
}

gradlePlugin {
    plugins {
        create("koinConvention") {
            id = "site.addzero.gradle.plugin.koin-convention"
            implementationClass = "site.addzero.gradle.plugin.KoinConventionPlugin"
        }
    }
}