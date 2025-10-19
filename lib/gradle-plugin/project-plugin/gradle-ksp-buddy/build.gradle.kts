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
        create(project.name) {
            id = "site.addzero.ksp-buddy"
            implementationClass = "site.addzero.gradle.plugin.kspbuddy.KspBuddyPlugin"
            displayName = "KSP Buddy Plugin"
        }
    }
}
