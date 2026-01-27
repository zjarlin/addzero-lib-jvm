import site.addzero.gradle.tool.configureJ8
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.tool.config.java)
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




