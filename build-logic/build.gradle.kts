
plugins {
    `kotlin-dsl`

    alias(libs.plugins.addzeroPublishBuddy)
}


//buildscript {
//    repositories {
//        gradlePluginPortal()
//    }
//    dependencies {
//        classpath(libs.gradlePlugin.buildkonfig)
//    }
//}
repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.org.graalvm.buildtools.native.gradle.plugin)
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    compileOnly(libs.snakeyaml)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.kotlinSerialization)
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.gradlePlugin.kotlinSpring)
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.springBoot)
    compileOnly(libs.gradlePlugin.jetbrainsCompose)

    // 添加gradleApi依赖以支持Gradle API
    gradleApi()


}
