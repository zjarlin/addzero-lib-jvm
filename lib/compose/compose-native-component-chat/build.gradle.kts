import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:compose:compose-native-component-button"))
                implementation(project(":lib:compose:compose-native-component-text"))
            }
        }
        jvmTest {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

val jvmTestCompilation = (kotlin.targets.getByName("jvm") as KotlinJvmTarget) .compilations.getByName("test")

tasks.register<JavaExec>("previewChat") {
    group = "application"
    description = "运行聊天组件桌面预览，用于本地手工验证。"
    dependsOn("jvmTestClasses")
    classpath(
        jvmTestCompilation.output.allOutputs,
        jvmTestCompilation.runtimeDependencyFiles,
    )
    mainClass.set("site.addzero.component.chat.preview.ChatPreviewMainKt")
    workingDir = project.projectDir
    val autoExitMillis = System.getProperty("chat.preview.autoExitMillis")
    if (!autoExitMillis.isNullOrBlank()) {
        systemProperty("chat.preview.autoExitMillis", autoExitMillis)
    }
    jvmArgs(
        "-Dsun.java2d.metal=false",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
    )
}
