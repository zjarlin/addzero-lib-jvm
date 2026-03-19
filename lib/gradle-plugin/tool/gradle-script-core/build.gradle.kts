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
//    `java-gradle-plugin`

}

dependencies {
    implementation(gradleApi())
}
