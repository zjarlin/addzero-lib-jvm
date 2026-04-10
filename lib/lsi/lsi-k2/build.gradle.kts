
plugins {
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"

    id("site.addzero.gradle.plugin.intellij-core")  version "2025.12.23"
}
val libs = versionCatalogs.named("libs")

afterEvaluate {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-lsi-intellij").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())

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
