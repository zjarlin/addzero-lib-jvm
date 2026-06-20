rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-20T09:53:02.583122
// Loaded: 1, Excluded: 0, Total: 1
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:tool-jvm:tool-spo")
include(":lib:tool-kmp:tool-enum")
include(":lib:tool-jvm:tool-cmd")
include(":lib:tool-jvm:tool-const")
include(":lib:tool-jvm:tool-date")
include(":lib:tool-jvm:tool-docker")
include(":lib:tool-jvm:tool-io")
include(":lib:tool-jvm:tool-ip")
include(":lib:tool-jvm:tool-math")
include(":lib:tool-jvm:tool-obj")
include(":lib:tool-jvm:tool-os")
include(":lib:tool-jvm:tool-printer")
include(":lib:tool-jvm:tool-reflection")
include(":lib:tool-jvm:tool-s3")
include(":lib:tool-jvm:tool-scheduler")

//include(":lib:api:api-openai") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao:yudao-dependencies") // excluded by Gradle Buddy
//include(":lib:tool-jvm:tool-jackson") // excluded by Gradle Buddy
//include(":lib:tool-jvm:tool-jackson-extra") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-common") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-env") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-captcha") // excluded by Gradle Buddy
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
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-rpc") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao3:yudao-dependencies") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common") // excluded by Gradle Buddy
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
