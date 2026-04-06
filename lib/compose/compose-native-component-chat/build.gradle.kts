import site.addzero.gradle.tool.registerJvmTestDesktopPreviewTask

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
    }
}

registerJvmTestDesktopPreviewTask(
    taskName = "previewChat",
    mainClass = "site.addzero.component.chat.preview.ChatPreviewMainKt",
    description = "运行聊天组件桌面预览，用于本地手工验证。",
    forwardedSystemProperties = listOf("chat.preview.autoExitMillis"),
)
