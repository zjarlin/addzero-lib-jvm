rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-06T08:14:48.257816
// Loaded: 14, Excluded: 0, Total: 14
include(":lib:tool-jvm:yudao:yudao-common")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-data-permission")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-tenant")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-excel")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-job")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mq")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mybatis")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-protection")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-redis")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-security")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-web")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-websocket")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:tool-jvm:yudao:yudao-common")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-web")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-security")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mybatis")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-redis")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mq")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-job")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-tenant")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-data-permission")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-excel")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-websocket")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-protection")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
