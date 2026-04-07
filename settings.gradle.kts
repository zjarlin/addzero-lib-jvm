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



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-07T11:35:02.269740
// Loaded: 9, Excluded: 0, Total: 9
include(":lib:compose:app-sidebar")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:ksp:metadata:controller2api-processor")
include(":lib:ksp:route:route-gradle-plugin")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<

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
