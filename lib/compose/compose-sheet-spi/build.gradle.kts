import site.addzero.gradle.tool.registerJvmTestJavaExecTask

plugins {
  id("site.addzero.buildlogic.kmp.cmp-jvmtest-entry")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
    }

    commonTest.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
      implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    }
  }
}

registerJvmTestJavaExecTask(
  taskName = "runSheetEngineScenario",
  mainClass = "site.addzero.component.sheet.preview.SheetEngineScenarioMainKt",
  description = "运行在线表格引擎独立场景验证。",
)
