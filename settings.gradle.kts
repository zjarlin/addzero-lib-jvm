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
// Generated at: 2026-03-28T13:04:04.118052
// Loaded: 1, Excluded: 0, Total: 1
include(":lib:compose:compose-icon-map")
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
