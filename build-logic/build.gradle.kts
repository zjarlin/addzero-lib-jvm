
plugins {
    `kotlin-dsl`
}
buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath(libs.gradlePlugin.buildkonfig)
  }
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}
dependencies {

//    implementation(projects.buildLogic)
    implementation(libs.snakeyaml)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradlePlugin.ktorfit)
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.jetbrainsCompose)
    implementation(libs.gradlePlugin.composeCompiler)
    compileOnly(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.kotlinSerialization)
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.kotlinSpring)
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.springBoot)
    implementation(libs.gradlePlugin.buildkonfig)
    implementation(libs.gradlePlugin.buildkonfig.cp)
//     implementation(libs.plugins.konfig)
//    implementation(files("com.codingfeline.buildkonfig:com.codingfeline.buildkonfig.gradle.plugin:0.17.1"))
//    com.codingfeline.buildkonfig:com.codingfeline.buildkonfig.gradle.plugin:0.17.1
}
