plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("catalogAutoresolve") {
            id = "site.addzero.gradle.plugin.catalog-autoresolve"
            implementationClass = "site.addzero.gradle.plugin.CatalogAutoresolvePlugin"
        }
    }
}
