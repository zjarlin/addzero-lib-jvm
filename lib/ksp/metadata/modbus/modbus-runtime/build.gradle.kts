plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

configurations.matching {
    it.name == "kotlinCompilerPluginClasspathWasmJsMain" ||
        it.name == "kotlinCompilerPluginClasspathWasmJsTest"
}.configureEach {
    exclude(group = "io.insert-koin", module = "koin-compiler-plugin")
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":lib:tool-jvm:tool-modbus"))
                implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
            }
        }
        jvmTest {
            kotlin.srcDir("src/test/kotlin")
            dependencies {
                implementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
            }
        }
    }
}

koinCompiler {
    userLogs = false
    debugLogs = false
    dslSafetyChecks = true
}
