plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradlePlugin.springBoot)
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.kotlinSpring)
    gradleApi()
}
