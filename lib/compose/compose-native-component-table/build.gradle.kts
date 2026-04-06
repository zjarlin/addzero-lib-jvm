import site.addzero.gradle.tool.registerJvmTestDesktopPreviewTask

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib:compose:compose-native-component-card"))
//            implementation(project(":lib:compose:compose-native-component"))
//            implementation(project(":lib:tool-kmp:tool-json"))
      implementation("site.addzero:compose-props-annotations:2025.09.30")
      api(project(":lib:compose:compose-native-component-table-core"))
    }
  }
}

registerJvmTestDesktopPreviewTask(
  taskName = "previewTable",
  mainClass = "site.addzero.component.table.preview.TablePreviewMainKt",
  description = "运行表格组件桌面预览，不参与正式发布产物。",
)
