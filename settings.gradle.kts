//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.all-object-jvm-static") version "+"
//    id("site.addzero.kcp.i18n") version "+"
//    id("site.addzero.kcp.multireceiver") version "+"
//    id("site.addzero.kcp.transform-overload") version "+"
//    id("site.addzero.kcp.spread-pack") version "+"
//  }
//}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}

@Suppress("unused")
val gradleModuleSleepDisableMarker = "site.addzero.gradle.plugin.modules-buddy"



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-07T11:35:02.269740
// Loaded: 9, Excluded: 0, Total: 9
include(":lib:compose:app-sidebar")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:ksp:metadata:controller2api-processor")
include(":lib:ksp:route:route-gradle-plugin")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<

// 为宿主配置树交互改造保留最小必要的 Compose 模块集合，便于本地组合编译验证。
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-workbench-shell")
include(":lib:tool-kmp:tool-tree")

//include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-gradle-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:kcp-i18n") // excluded by Gradle Buddy
//include(":lib:kcp:kcp-i18n-gradle-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:kcp-i18n-runtime") // excluded by Gradle Buddy
//include(":lib:kcp:multireceiver:kcp-multireceiver-annotations") // excluded by Gradle Buddy
//include(":lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:multireceiver:kcp-multireceiver-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:singleton-adapter-kcp") // excluded by Gradle Buddy
//include(":lib:kcp:spread-pack:kcp-spread-pack-annotations") // excluded by Gradle Buddy
//include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:spread-pack:kcp-spread-pack-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:transform-overload:kcp-transform-overload-annotations") // excluded by Gradle Buddy
//include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:transform-overload:kcp-transform-overload-plugin") // excluded by Gradle Buddy

include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-gradle-plugin")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin")
include(":lib:kcp:kcp-i18n")
include(":lib:kcp:kcp-i18n-gradle-plugin")
include(":lib:kcp:kcp-i18n-runtime")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")
include(":lib:kcp:singleton-adapter-kcp")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")

// KSP threshold-rule migration: keep retained consumer plugins and the plugin smoke/audit
// test module visible to the same-repo build without waking unrelated legacy processor trees.
include(":lib:ksp:published-gradle-plugin-tests")
include(":lib:ksp:route:route-processor")
include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
include(":lib:ksp:metadata:gen-reified:gen-reified-gradle-plugin")
include(":lib:ksp:metadata:ioc:ioc-gradle-plugin")
include(":lib:ksp:metadata:jimmer-entity-external-gradle-plugin")
include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-gradle-plugin")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-mqtt-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-runtime")
include(":lib:ksp:metadata:modbus:modbus-codegen-model")
include(":lib:ksp:metadata:modbus:modbus-codegen-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-keil-sync")
include(":lib:ksp:metadata:modbus:modbus-ksp-markdown")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
include(":lib:ksp:metadata:modbus:modbus-ksp-mqtt")
include(":lib:ksp:metadata:multireceiver-gradle-plugin")
include(":lib:ksp:metadata:singleton-adapter-gradle-plugin")
include(":lib:ksp:metadata:spring2ktor-server-gradle-plugin")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-jvm:tool-modbus")
