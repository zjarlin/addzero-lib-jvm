//import site.addzero.gradle.tool.configureJ8
//buildscript {
//    repositories {
//        mavenLocal()
//        mavenCentral()
//    }
//    dependencies {
//        classpath("site.addzero:gradle-tool-config-java:0.0.674")
//    }
//}
//configureJ8("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id("publish-convention")

}
dependencies {
gradleApi()
    implementation(libs.gradlePlugin.mavenPublish)
   implementation("site.addzero:gradle-script-core:2025.10.20")
}
