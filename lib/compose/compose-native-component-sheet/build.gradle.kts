import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:compose-sheet-spi"))
    }

    jvmTest.dependencies {
      implementation(compose.desktop.currentOs)
    }
  }
}

val jvmTestCompilation = (kotlin.targets.getByName("jvm") as KotlinJvmTarget)
  .compilations.getByName("test")

tasks.register<JavaExec>("previewSheetWorkbench") {
  group = "application"
  description = "运行在线表格工作台桌面预览。"
  dependsOn("jvmTestClasses")
  classpath(
    jvmTestCompilation.output.allOutputs,
    jvmTestCompilation.runtimeDependencyFiles,
  )
  mainClass.set("site.addzero.component.sheet.preview.SheetWorkbenchPreviewMainKt")
  workingDir = project.projectDir
  val autoExitMillis = System.getProperty("sheet.preview.autoExitMillis")
  if (!autoExitMillis.isNullOrBlank()) {
    systemProperty("sheet.preview.autoExitMillis", autoExitMillis)
  }
  jvmArgs(
    "-Dsun.java2d.metal=false",
    "-Dfile.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8",
  )
}
