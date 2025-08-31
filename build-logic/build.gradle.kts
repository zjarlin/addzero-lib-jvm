import org.jetbrains.kotlin.gradle.internal.backend.common.serialization.metadata.DynamicTypeDeserializer.id

plugins {
    `kotlin-dsl`
}


repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}
dependencies {
//    api(gradleApi())

    implementation(libs.snakeyaml)
//    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

//    implementation(libs.gradlePlugin.ktorfit)
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.jetbrainsCompose)
    implementation(libs.gradlePlugin.composeCompiler)
    compileOnly(libs.gradlePlugin.kotlin)
    // 确保添加序列化插件依赖
    implementation(libs.gradlePlugin.kotlinSerialization) // 与Kotlin版本一致
//    implementation("org.jetbrains.kotlin.plugin.serialization:$kotlinVersion")
    implementation(libs.gradlePlugin.mavenPublish)
//    implementation("com.vanniktech:gradle-maven-publish-plugin:+")
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.kotlinSpring)
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.springBoot)

}
