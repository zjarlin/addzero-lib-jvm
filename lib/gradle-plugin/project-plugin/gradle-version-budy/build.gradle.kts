import site.addzero.gradle.tool.configureJdk

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
    }
}
configureJdk("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id("publish-convention")

}
dependencies {
    implementation(gradleApi())
    implementation(libs.site.addzero.gradle.script.core)
    implementation(libs.site.addzero.tool.api.maven)

}
