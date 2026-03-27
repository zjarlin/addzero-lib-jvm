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
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(gradleApi())
    implementation(catalogLibs.findLibrary("site-addzero-gradle-script-core").get())
    implementation(catalogLibs.findLibrary("site-addzero-tool-api-maven").get())

}
