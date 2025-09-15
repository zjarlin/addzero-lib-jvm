plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id()
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
}

val pluginName = project.name
//BuildSettings


gradlePlugin {
    plugins {
        create(pluginName) {
            id = "ksp-buddy"
            implementationClass = "site.addzero.gradle.plugin.kspbuddy.KspBuddyPlugin"
            displayName = "KSP Buddy Plugin"
        }
    }
}
