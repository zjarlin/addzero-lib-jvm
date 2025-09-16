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

//BuildSettings


gradlePlugin {
    plugins {
        create(project.name) {
            id = "ksp-buddy"
            implementationClass = "site.addzero.gradle.plugin.kspbuddy.KspBuddyPlugin"
            displayName = "KSP Buddy Plugin"
        }
    }
}
