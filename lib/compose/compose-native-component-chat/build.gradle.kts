import site.addzero.gradle.tool.registerJvmTestDesktopPreviewTask

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-text").get())
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
