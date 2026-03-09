import site.addzero.gradle.tool.configureJdk

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
    }
}
configureJdk("17")


//version = "2026.03.02"

dependencies {
    gradleApi()
    implementation(libs.com.vanniktech.gradle.maven.publish.plugin)
    implementation(libs.site.addzero.gradle.script.core)

}
