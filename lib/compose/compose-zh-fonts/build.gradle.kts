import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

val libs = versionCatalogs.named("libs")
val kotlinVersionForComposeLib = libs.findVersion("kotlin").get().requiredVersion

configurations.configureEach {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersionForComposeLib")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("org-jetbrains-compose-ui-ui").get())
            api(libs.findLibrary("org-jetbrains-compose-material3-material3").get())
            api(libs.findLibrary("org-jetbrains-compose-components-components-resources").get())
        }
        wasmJsMain.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "site.addzero.compose.zh.fonts.generated.resources"
    generateResClass = always
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("compose-zh-fonts")
            description.set("Compose Multiplatform 中文字体支持模块，重点解决 Wasm 场景下的中文字体加载。")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("zjarlin")
                    name.set("zjarlin")
                }
            }
        }
    }
}
