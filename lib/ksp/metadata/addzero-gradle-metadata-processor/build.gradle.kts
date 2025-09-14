plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "site.addzero.gradle"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// 设置兼容的JDK版本
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(8))
//    }
//}
//
//kotlin {
//    jvmToolchain(8)
//}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("gradleKspConfigPlugin") {
            id = "site.addzero.gradle.ksp.config"
            implementationClass = "site.addzero.gradle.plugin.GradleKspConfigPlugin"
            displayName = "Gradle KSP Config Plugin"
            description = "Automatically generates strongly-typed configuration objects for KSP settings in all modules"
        }
    }
}
