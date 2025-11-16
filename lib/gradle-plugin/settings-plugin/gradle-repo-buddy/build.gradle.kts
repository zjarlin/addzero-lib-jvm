import site.addzero.gradle.tool.configureJ8
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("site.addzero:gradle-tool-config-java:0.0.674")
    }
}
configureJ8("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        register(project.name) {
            id = "site.addzero.repo-buddy"
            implementationClass = "RepoConfigPlugin"
        }
    }
}
