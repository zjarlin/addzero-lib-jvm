import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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
    jvmTest.dependencies {
      implementation(compose.desktop.currentOs)
    }
  }
}

val jvmTestCompilation = (kotlin.targets.getByName("jvm") as KotlinJvmTarget)
  .compilations.getByName("test")

tasks.register<JavaExec>("previewTable") {
  group = "application"
  description = "运行表格组件桌面预览，不参与正式发布产物。"
  dependsOn("jvmTestClasses")
  classpath(
    jvmTestCompilation.output.allOutputs,
    jvmTestCompilation.runtimeDependencyFiles,
  )
  mainClass.set("site.addzero.component.table.preview.TablePreviewMainKt")
  workingDir = project.projectDir
  jvmArgs(
    "-Dsun.java2d.metal=false",
    "-Dfile.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8",
  )
}
