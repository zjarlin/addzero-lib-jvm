import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
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

val jvmTestCompilation = (kotlin.targets.getByName("jvm") as KotlinJvmTarget)
  .compilations.getByName("test")

tasks.register<JavaExec>("runSheetEngineScenario") {
  group = "application"
  description = "运行在线表格引擎独立场景验证。"
  dependsOn("jvmTestClasses")
  classpath(
    jvmTestCompilation.output.allOutputs,
    jvmTestCompilation.runtimeDependencyFiles,
  )
  mainClass.set("site.addzero.component.sheet.preview.SheetEngineScenarioMainKt")
  workingDir = project.projectDir
  jvmArgs(
    "-Dfile.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8",
  )
}
