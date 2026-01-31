import site.addzero.gradle.plugin.KotlinConventionPlugin

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+" version "+"

    id("site.addzero.gradle.plugin.intellij-core")  version "2025.12.23"
}

dependencies {
    implementation(libs.lsi.core)
    implementation(libs.lsi.intellij)
    implementation(libs.tool.str)

    // K2 Analysis API 通过 Kotlin 插件捆绑提供
//    intellijPlatform {
//        bundledPlugin("org.jetbrains.kotlin")
//    }
}

// 启用 K2 Analysis API 实验性功能
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=org.jetbrains.kotlin.analysis.api.KaExperimentalApi",
            "-opt-in=org.jetbrains.kotlin.analysis.api.KaNonPublicApi"
        )
    }
}
