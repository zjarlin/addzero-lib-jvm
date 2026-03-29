//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.i18n") version "2026.10329.10127"
//    id("site.addzero.kcp.multireceiver") version "2026.10329.10127"
//    id("site.addzero.kcp.transform-overload") version "2026.10329.10127"
//  }
//}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}

val localBuildLogicDir = file("checkouts/build-logic")
val sharedKlibBuildLogicDir = file("../addzero-lib-kmp/lib-git/build-logic-klibs")
val activeBuildLogicDir = when {
  localBuildLogicDir.resolve("src/main/kotlin").isDirectory -> localBuildLogicDir
  sharedKlibBuildLogicDir.resolve("src/main/kotlin").isDirectory -> sharedKlibBuildLogicDir
  else -> localBuildLogicDir
}

includeBuild(activeBuildLogicDir) {
  name = "addzero-lib-jvm-build-logic"
}

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-29T19:47:13.583979
// Loaded: 39, Excluded: 0, Total: 39
include(":lib:api:api-music-spi")
include(":lib:api:api-netease")
include(":lib:api:api-qqmusic")
include(":lib:api:api-suno")
include(":lib:compose:compose-icon-map")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:kbox-plugin-api")
include(":lib:ksp:logger-api")
include(":lib:ksp:metadata:apiprovider-gradle-plugin")
include(":lib:ksp:metadata:apiprovider-processor")
include(":lib:ksp:metadata:controller2api-gradle-plugin")
include(":lib:ksp:metadata:controller2api-idea-plugin")
include(":lib:ksp:metadata:controller2api-processor")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper")
include(":lib:ksp:metadata:singleton-adapter-api")
include(":lib:openapi-codegen")
include(":lib:tool-jvm:network-call:browser:tool-api-browser-automation")
include(":lib:tool-jvm:network-call:chat:api-models-chat")
include(":lib:tool-jvm:network-call:music:api-music-spi")
include(":lib:tool-jvm:network-call:music:api-netease")
include(":lib:tool-jvm:network-call:music:api-netease-semantic-impl")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-jvm:network-call:tool-api-ocr")
include(":lib:tool-jvm:network-call:tool-api-payment")
include(":lib:tool-jvm:network-call:tool-api-soft-download")
include(":lib:tool-jvm:network-call:tool-api-temp-mail")
include(":lib:tool-jvm:network-call:tool-api-translate")
include(":lib:tool-jvm:network-call:tool-api-tyc")
include(":lib:tool-jvm:network-call:tool-api-tyc-hw")
include(":lib:tool-jvm:network-call:tool-api-video-parse")
include(":lib:tool-jvm:network-call:tool-api-video-search-and-download")
include(":lib:tool-jvm:network-call:tool-api-weather")
include(":lib:tool-jvm:tool-api-jvm")
include(":lib:tool-kmp:ktor:starter:starter-openapi")
include(":lib:tool-kmp:ktor:starter:starter-spi")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:ksp:route:route-core")
project(":lib:ksp:route:route-core").projectDir = file("lib/ksp/route/route-core")

include(":lib:ksp:route:route-processor")
project(":lib:ksp:route:route-processor").projectDir = file("lib/ksp/route/route-processor")

include(":lib:ksp:jdbc2metadata:jdbc2controller-processor")
project(":lib:ksp:jdbc2metadata:jdbc2controller-processor").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2controller-processor")

include(":lib:ksp:jdbc2metadata:jdbc2entity-processor")
project(":lib:ksp:jdbc2metadata:jdbc2entity-processor").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2entity-processor")

include(":lib:ksp:jdbc2metadata:jdbc2enum-processor")
project(":lib:ksp:jdbc2metadata:jdbc2enum-processor").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2enum-processor")

include(":lib:ksp:logger-processor")
project(":lib:ksp:logger-processor").projectDir = file("lib/ksp/logger-processor")

include(":lib:ksp:logger-api")
project(":lib:ksp:logger-api").projectDir = file("lib/ksp/logger-api")

include(":lib:ksp:logger-implementation")
project(":lib:ksp:logger-implementation").projectDir = file("lib/ksp/logger-implementation")

include(":lib:ksp:metadata:apiprovider-processor")
project(":lib:ksp:metadata:apiprovider-processor").projectDir =
  file("lib/ksp/metadata/apiprovider-processor")

include(":lib:ksp:metadata:compose-props:compose-props-annotations")
project(":lib:ksp:metadata:compose-props:compose-props-annotations").projectDir =
  file("lib/ksp/metadata/compose-props/compose-props-annotations")

include(":lib:ksp:metadata:compose-props:compose-props-processor")
project(":lib:ksp:metadata:compose-props:compose-props-processor").projectDir =
  file("lib/ksp/metadata/compose-props/compose-props-processor")

include(":lib:ksp:metadata:controller2api-processor")
project(":lib:ksp:metadata:controller2api-processor").projectDir =
  file("lib/ksp/metadata/controller2api-processor")

include(":lib:ksp:metadata:controller2feign-processor")
project(":lib:ksp:metadata:controller2feign-processor").projectDir =
  file("lib/ksp/metadata/controller2feign-processor")

include(":lib:ksp:metadata:controller2iso2dataprovider-processor")
project(":lib:ksp:metadata:controller2iso2dataprovider-processor").projectDir =
  file("lib/ksp/metadata/controller2iso2dataprovider-processor")

include(":lib:ksp:metadata:enum-processor")
project(":lib:ksp:metadata:enum-processor").projectDir = file("lib/ksp/metadata/enum-processor")

include(":lib:ksp:metadata:gen-reified:gen-reified-processor")
project(":lib:ksp:metadata:gen-reified:gen-reified-processor").projectDir =
  file("lib/ksp/metadata/gen-reified/gen-reified-processor")

include(":lib:ksp:metadata:ioc:ioc-core")
project(":lib:ksp:metadata:ioc:ioc-core").projectDir = file("lib/ksp/metadata/ioc/ioc-core")

include(":lib:ksp:metadata:ioc:ioc-processor")
project(":lib:ksp:metadata:ioc:ioc-processor").projectDir =
  file("lib/ksp/metadata/ioc/ioc-processor")

include(":lib:ksp:metadata:jimmer-entity-external-processor")
project(":lib:ksp:metadata:jimmer-entity-external-processor").projectDir =
  file("lib/ksp/metadata/jimmer-entity-external-processor")

include(":lib:ksp:metadata:jimmer-entity-spi")
project(":lib:ksp:metadata:jimmer-entity-spi").projectDir =
  file("lib/ksp/metadata/jimmer-entity-spi")

include(":lib:ksp:metadata:entity2iso-processor")
project(":lib:ksp:metadata:entity2iso-processor").projectDir =
  file("lib/ksp/metadata/entity2iso-processor")

include(":lib:ksp:metadata:entity2form:entity2form-processor")
project(":lib:ksp:metadata:entity2form:entity2form-processor").projectDir =
  file("lib/ksp/metadata/entity2form/entity2form-processor")

include(":lib:ksp:metadata:entity2mcp-processor")
project(":lib:ksp:metadata:entity2mcp-processor").projectDir =
  file("lib/ksp/metadata/entity2mcp-processor")

include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor")
project(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor").projectDir =
  file("lib/ksp/metadata/ksp-dsl-builder/ksp-dsl-builder-processor")

include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api")
project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api").projectDir =
  file("lib/ksp/metadata/method-semanticizer/method-semanticizer-api")

include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor")
project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor").projectDir =
  file("lib/ksp/metadata/method-semanticizer/method-semanticizer-processor")

include(":lib:ksp:metadata:multireceiver-processor")
project(":lib:ksp:metadata:multireceiver-processor").projectDir =
  file("lib/ksp/metadata/multireceiver-processor")

include(":lib:ksp:metadata:singleton-adapter-api")
project(":lib:ksp:metadata:singleton-adapter-api").projectDir =
  file("lib/ksp/metadata/singleton-adapter-api")

include(":lib:ksp:metadata:singleton-adapter-processor")
project(":lib:ksp:metadata:singleton-adapter-processor").projectDir =
  file("lib/ksp/metadata/singleton-adapter-processor")

include(":lib:ksp:metadata:spring2ktor-server-core")
project(":lib:ksp:metadata:spring2ktor-server-core").projectDir =
  file("lib/ksp/metadata/spring2ktor-server-core")

include(":lib:ksp:metadata:spring2ktor-server-processor")
project(":lib:ksp:metadata:spring2ktor-server-processor").projectDir =
  file("lib/ksp/metadata/spring2ktor-server-processor")

include(":lib:kcp:kcp-i18n")
project(":lib:kcp:kcp-i18n").projectDir = file("lib/kcp/kcp-i18n")

include(":lib:kcp:kcp-i18n-runtime")
project(":lib:kcp:kcp-i18n-runtime").projectDir = file("lib/kcp/kcp-i18n-runtime")

include(":lib:kcp:kcp-i18n-gradle-plugin")
project(":lib:kcp:kcp-i18n-gradle-plugin").projectDir = file("lib/kcp/kcp-i18n-gradle-plugin")

include(":lib:kcp:kcp-i18n-idea-plugin")
project(":lib:kcp:kcp-i18n-idea-plugin").projectDir = file("lib/kcp/kcp-i18n-idea-plugin")

include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
project(":lib:kcp:multireceiver:kcp-multireceiver-annotations").projectDir =
  file("lib/kcp/multireceiver/kcp-multireceiver-annotations")

include(":lib:lsi:lsi-core")
project(":lib:lsi:lsi-core").projectDir = file("lib/lsi/lsi-core")

include(":lib:lsi:lsi-ksp")
project(":lib:lsi:lsi-ksp").projectDir = file("lib/lsi/lsi-ksp")

include(":lib:compose:compose-zh-fonts")
project(":lib:compose:compose-zh-fonts").projectDir = file("lib/compose/compose-zh-fonts")

include(":lib:compose:compose-native-component-high-level")
project(":lib:compose:compose-native-component-high-level").projectDir =
  file("lib/compose/compose-native-component-high-level")

include(":lib:compose:compose-native-component-button")
project(":lib:compose:compose-native-component-button").projectDir =
  file("lib/compose/compose-native-component-button")

include(":lib:compose:compose-native-component-searchbar")
project(":lib:compose:compose-native-component-searchbar").projectDir =
  file("lib/compose/compose-native-component-searchbar")

include(":lib:compose:compose-native-component-tree")
project(":lib:compose:compose-native-component-tree").projectDir =
  file("lib/compose/compose-native-component-tree")

include(":lib:tool-kmp:tool-coll")
project(":lib:tool-kmp:tool-coll").projectDir = file("lib/tool-kmp/tool-coll")

include(":lib:tool-kmp:tool-tree")
project(":lib:tool-kmp:tool-tree").projectDir = file("lib/tool-kmp/tool-tree")

include(":lib:ksp:metadata:modbus:modbus-runtime")
project(":lib:ksp:metadata:modbus:modbus-runtime").projectDir = file("lib/ksp/metadata/modbus/modbus-runtime")

include(":lib:ksp:metadata:modbus:modbus-ksp-core")
project(":lib:ksp:metadata:modbus:modbus-ksp-core").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-core")

include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
project(":lib:ksp:metadata:modbus:modbus-ksp-rtu").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-rtu")

include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
project(":lib:ksp:metadata:modbus:modbus-ksp-tcp").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-tcp")

include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
project(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base").projectDir =
  file("lib/gradle-plugin/project-plugin/gradle-ksp-consumer-base")

include(":lib:ksp:route:route-gradle-plugin")
project(":lib:ksp:route:route-gradle-plugin").projectDir = file("lib/ksp/route/route-gradle-plugin")

include(":lib:ksp:jdbc2metadata:jdbc2controller-gradle-plugin")
project(":lib:ksp:jdbc2metadata:jdbc2controller-gradle-plugin").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2controller-gradle-plugin")

include(":lib:ksp:jdbc2metadata:jdbc2entity-gradle-plugin")
project(":lib:ksp:jdbc2metadata:jdbc2entity-gradle-plugin").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2entity-gradle-plugin")

include(":lib:ksp:jdbc2metadata:jdbc2enum-gradle-plugin")
project(":lib:ksp:jdbc2metadata:jdbc2enum-gradle-plugin").projectDir =
  file("lib/ksp/jdbc2metadata/jdbc2enum-gradle-plugin")

include(":lib:ksp:logger-gradle-plugin")
project(":lib:ksp:logger-gradle-plugin").projectDir = file("lib/ksp/logger-gradle-plugin")

include(":lib:ksp:metadata:apiprovider-gradle-plugin")
project(":lib:ksp:metadata:apiprovider-gradle-plugin").projectDir =
  file("lib/ksp/metadata/apiprovider-gradle-plugin")

include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
project(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin").projectDir =
  file("lib/ksp/metadata/compose-props/compose-props-gradle-plugin")

include(":lib:ksp:metadata:controller2api-gradle-plugin")
project(":lib:ksp:metadata:controller2api-gradle-plugin").projectDir =
  file("lib/ksp/metadata/controller2api-gradle-plugin")

include(":lib:ksp:metadata:controller2api-idea-plugin")
project(":lib:ksp:metadata:controller2api-idea-plugin").projectDir =
  file("lib/ksp/metadata/controller2api-idea-plugin")

include(":lib:ksp:metadata:controller2feign-gradle-plugin")
project(":lib:ksp:metadata:controller2feign-gradle-plugin").projectDir =
  file("lib/ksp/metadata/controller2feign-gradle-plugin")

include(":lib:ksp:metadata:controller2iso2dataprovider-gradle-plugin")
project(":lib:ksp:metadata:controller2iso2dataprovider-gradle-plugin").projectDir =
  file("lib/ksp/metadata/controller2iso2dataprovider-gradle-plugin")

include(":lib:ksp:metadata:enum-gradle-plugin")
project(":lib:ksp:metadata:enum-gradle-plugin").projectDir =
  file("lib/ksp/metadata/enum-gradle-plugin")

include(":lib:ksp:metadata:gen-reified:gen-reified-gradle-plugin")
project(":lib:ksp:metadata:gen-reified:gen-reified-gradle-plugin").projectDir =
  file("lib/ksp/metadata/gen-reified/gen-reified-gradle-plugin")

include(":lib:ksp:metadata:ioc:ioc-gradle-plugin")
project(":lib:ksp:metadata:ioc:ioc-gradle-plugin").projectDir =
  file("lib/ksp/metadata/ioc/ioc-gradle-plugin")

include(":lib:ksp:metadata:jimmer-entity-external-gradle-plugin")
project(":lib:ksp:metadata:jimmer-entity-external-gradle-plugin").projectDir =
  file("lib/ksp/metadata/jimmer-entity-external-gradle-plugin")

include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-gradle-plugin")
project(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-gradle-plugin").projectDir =
  file("lib/ksp/metadata/ksp-dsl-builder/ksp-dsl-builder-gradle-plugin")

include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-gradle-plugin")
project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-gradle-plugin").projectDir =
  file("lib/ksp/metadata/method-semanticizer/method-semanticizer-gradle-plugin")

include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
project(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin").projectDir =
  file("lib/ksp/metadata/modbus/modbus-rtu-gradle-plugin")

include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin")
project(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin").projectDir =
  file("lib/ksp/metadata/modbus/modbus-tcp-gradle-plugin")

include(":lib:ksp:metadata:multireceiver-gradle-plugin")
project(":lib:ksp:metadata:multireceiver-gradle-plugin").projectDir =
  file("lib/ksp/metadata/multireceiver-gradle-plugin")

include(":lib:ksp:metadata:singleton-adapter-gradle-plugin")
project(":lib:ksp:metadata:singleton-adapter-gradle-plugin").projectDir =
  file("lib/ksp/metadata/singleton-adapter-gradle-plugin")

include(":lib:ksp:metadata:spring2ktor-server-gradle-plugin")
project(":lib:ksp:metadata:spring2ktor-server-gradle-plugin").projectDir =
  file("lib/ksp/metadata/spring2ktor-server-gradle-plugin")

include(":lib:ksp:metadata:kcloud")
project(":lib:ksp:metadata:kcloud").projectDir = file("lib/ksp/metadata/kcloud")

include(":lib:ksp:published-gradle-plugin-tests")
project(":lib:ksp:published-gradle-plugin-tests").projectDir = file("lib/ksp/published-gradle-plugin-tests")

include(":lib:tool-kmp:network-starter")
project(":lib:tool-kmp:network-starter").projectDir = file("lib/tool-kmp/network-starter")

include(":lib:tool-kmp:ktor:ktor-banner")
project(":lib:tool-kmp:ktor:ktor-banner").projectDir = file("lib/tool-kmp/ktor/ktor-banner")

include(":lib:tool-kmp:ktor:starter:starter-spi")
project(":lib:tool-kmp:ktor:starter:starter-spi").projectDir = file("lib/tool-kmp/ktor/starter/starter-spi")

include(":lib:tool-kmp:ktor:starter:starter-banner")
project(":lib:tool-kmp:ktor:starter:starter-banner").projectDir = file("lib/tool-kmp/ktor/starter/starter-banner")

include(":lib:tool-kmp:ktor:starter:starter-koin")
project(":lib:tool-kmp:ktor:starter:starter-koin").projectDir = file("lib/tool-kmp/ktor/starter/starter-koin")

include(":lib:tool-kmp:ktor:starter:starter-openapi")
project(":lib:tool-kmp:ktor:starter:starter-openapi").projectDir = file("lib/tool-kmp/ktor/starter/starter-openapi")

include(":lib:tool-kmp:ktor:starter:starter-serialization")
project(":lib:tool-kmp:ktor:starter:starter-serialization").projectDir =
  file("lib/tool-kmp/ktor/starter/starter-serialization")

include(":lib:tool-kmp:ktor:starter:starter-statuspages")
project(":lib:tool-kmp:ktor:starter:starter-statuspages").projectDir =
  file("lib/tool-kmp/ktor/starter/starter-statuspages")

include(":lib:tool-kmp:ktor:starter:starter-flyway")
project(":lib:tool-kmp:ktor:starter:starter-flyway").projectDir =
  file("lib/tool-kmp/ktor/starter/starter-flyway")

include(":lib:tool-kmp:ktor:plugin:ktor-jimmer-plugin")
project(":lib:tool-kmp:ktor:plugin:ktor-jimmer-plugin").projectDir =
  file("lib/tool-kmp/ktor/plugin/ktor-jimmer-plugin")

include(":lib:tool-kmp:ktor:plugin:ktor-s3-plugin")
project(":lib:tool-kmp:ktor:plugin:ktor-s3-plugin").projectDir =
  file("lib/tool-kmp/ktor/plugin/ktor-s3-plugin")

include(":lib:api:api-music-spi")
project(":lib:api:api-music-spi").projectDir = file("lib/api/api-music-spi")

include(":lib:api:api-netease")
project(":lib:api:api-netease").projectDir = file("lib/api/api-netease")

include(":lib:api:api-qqmusic")
project(":lib:api:api-qqmusic").projectDir = file("lib/api/api-qqmusic")

include(":lib:api:api-suno")
project(":lib:api:api-suno").projectDir = file("lib/api/api-suno")

include(":lib:compose:app-sidebar")
project(":lib:compose:app-sidebar").projectDir = file("lib/compose/app-sidebar")

include(":lib:compose:glass-components")
project(":lib:compose:glass-components").projectDir = file("lib/compose/glass-components")

include(":lib:compose:liquid-glass")
project(":lib:compose:liquid-glass").projectDir = file("lib/compose/liquid-glass")

include(":lib:compose:media-playlist-player")
project(":lib:compose:media-playlist-player").projectDir = file("lib/compose/media-playlist-player")

include(":lib:compose:scaffold-spi")
project(":lib:compose:scaffold-spi").projectDir = file("lib/compose/scaffold-spi")

include(":lib:config-center:client")
project(":lib:config-center:client").projectDir = file("lib/config-center/client")

include(":lib:config-center:ktor")
project(":lib:config-center:ktor").projectDir = file("lib/config-center/ktor")

include(":lib:config-center:runtime-jvm")
project(":lib:config-center:runtime-jvm").projectDir = file("lib/config-center/runtime-jvm")

include(":lib:config-center:spec")
project(":lib:config-center:spec").projectDir = file("lib/config-center/spec")

include(":lib:kbox-core")
project(":lib:kbox-core").projectDir = file("lib/kbox-core")

include(":lib:kbox-plugin-api")
project(":lib:kbox-plugin-api").projectDir = file("lib/kbox-plugin-api")

include(":lib:kbox-plugin-runtime")
project(":lib:kbox-plugin-runtime").projectDir = file("lib/kbox-plugin-runtime")

include(":lib:kbox-ssh")
project(":lib:kbox-ssh").projectDir = file("lib/kbox-ssh")

include(":lib:kcloud-core")
project(":lib:kcloud-core").projectDir = file("lib/kcloud-core")

include(":lib:kcloud-paths")
project(":lib:kcloud-paths").projectDir = file("lib/kcloud-paths")

include(":lib:openapi-codegen")
project(":lib:openapi-codegen").projectDir = file("lib/openapi-codegen")

include(":lib:spec:system-spec")
project(":lib:spec:system-spec").projectDir = file("lib/spec/system-spec")
