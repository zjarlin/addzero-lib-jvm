rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-11T14:31:54.338841
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:tool-kmp:koog")
include(":lib:tool-kmp:tool-config-center-client")
include(":lib:tool-kmp:tool-json")
// <<< Gradle Module Sleep: End Of Block <<<

//include(":lib:tool-jvm:yudao:yudao-common") // excluded by Gradle Buddy
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
