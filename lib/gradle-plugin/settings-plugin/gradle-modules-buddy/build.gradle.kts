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
    implementation(projects.lib.gradlePlugin.gradleTool)
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = "site.addzero.modules-buddy"
            implementationClass = "site.addzero.gradle.plugin.automodules.AutoModulesPlugin"
        }
    }
}
