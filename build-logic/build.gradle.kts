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
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.org.graalvm.buildtools.native.gradle.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.snakeyaml)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.kotlinSerialization)
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.gradlePlugin.kotlinSpring)
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.springBoot)


//    kmp
    implementation(libs.gradlePlugin.jetbrainsCompose)


}
