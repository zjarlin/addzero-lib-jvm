rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-18T10:54:05.756970
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:tool-jvm:tool-spctx")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common")
include(":lib:tool-jvm:yudao:yudao-common")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-captcha")
// <<< Gradle Module Sleep: End Of Block <<<

//include(":lib:api:api-openai") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao:yudao-dependencies") // excluded by Gradle Buddy
include(":lib:tool-jvm:tool-jackson")
include(":lib:tool-jvm:tool-jackson-extra")
include(":lib:tool-jvm:yudao:yudao-common")
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-env") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-captcha")
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-web") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-security") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mybatis") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-redis") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-mq") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-job") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-tenant") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-data-permission") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-excel") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-websocket") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-protection") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-rpc") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao3:yudao-dependencies") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common")
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-env") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-web") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-security") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mybatis") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-redis") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mq") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-job") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-tenant") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-data-permission") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-excel") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-websocket") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-monitor") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-protection") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-ip") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-rpc") // excluded by Gradle Buddy
