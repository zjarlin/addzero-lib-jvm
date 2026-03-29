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

configureJdk("11")

plugins {
    `kotlin-dsl`
}

val catalogLibs = versionCatalogs.named("libs")

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-gradle-plugin").get())
    implementation(catalogLibs.findLibrary("com-google-devtools-ksp-com-google-devtools-ksp-gradle-plugin").get())
    testImplementation(kotlin("test"))
}
