import site.addzero.gradle.tool.registerJvmTestDesktopPreviewTask

plugins {
  id("site.addzero.buildlogic.kmp.cmp-jvmtest-entry")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:compose-sheet-spi"))
    }
  }
}

registerJvmTestDesktopPreviewTask(
  taskName = "previewSheetWorkbench",
  mainClass = "site.addzero.component.sheet.preview.SheetWorkbenchPreviewMainKt",
  description = "运行在线表格工作台桌面预览。",
  forwardedSystemProperties = listOf("sheet.preview.autoExitMillis"),
)
