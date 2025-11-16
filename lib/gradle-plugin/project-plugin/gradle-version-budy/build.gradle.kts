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
//    id("publish-convention")

}
dependencies {
    implementation(gradleApi())
    implementation(projects.lib.gradlePlugin.gradleScriptCore)
    implementation(projects.lib.toolJvm.networkCall.toolApiMaven)


}
