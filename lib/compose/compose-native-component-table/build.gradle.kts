import site.addzero.gradle.tool.registerJvmTestDesktopPreviewTask

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("site-addzero-compose-native-component-card").get())
//            implementation("site.addzero:compose-native-component:2025.09.30")
//            implementation("site.addzero:tool-json:2026.02.04")
      implementation(libs.findLibrary("site-addzero-compose-props-annotations").get())
      api(libs.findLibrary("site-addzero-compose-native-component-table-core").get())
    }
  }
}

registerJvmTestDesktopPreviewTask(
  taskName = "previewTable",
  mainClass = "site.addzero.component.table.preview.TablePreviewMainKt",
  description = "运行表格组件桌面预览，不参与正式发布产物。",
)
