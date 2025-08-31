plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("publish-convention")
}

group = "io.gitee.zjarlin"
// 从根项目的gradle.properties读取版本号
version = rootProject.version

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
        create("autoModulesPlugin") {
            id = "io.gitee.zjarlin.auto-modules"
            implementationClass = "com.addzero.gradle.plugin.automodules.AutoModulesPlugin"
            displayName = "Auto Modules Plugin"
            description = "Automatically discover and include Gradle modules by scanning project directories"
        }
    }
}
