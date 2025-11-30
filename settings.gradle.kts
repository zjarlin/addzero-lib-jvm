pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "addzero-lib-jvm"

// 包含所有子项目
include(":lib:gradle-plugin:settings-plugin:gradle-checkout-repos")
include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
include(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
include(":lib:gradle-plugin:gradle-script")
include(":lib:gradle-plugin:gradle-script-core")
include(":lib:gradle-plugin:gradle-tool")
include(":lib:gradle-plugin:gradle-tool-config-java")
include(":lib:gradle-plugin:project-plugin")
