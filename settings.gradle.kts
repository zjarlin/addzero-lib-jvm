rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-22T11:49:40.784959
// Loaded: 21, Excluded: 0, Total: 21
include(":lib:apt:dict-trans:apt-dict-trans-processor")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:gradle-plugin:auto-jvmname")
include(":lib:gradle-plugin:tool:catalog-autoresolve")
include(":lib:tool-jvm:database:mybatis-auto-wrapper")
include(":lib:tool-jvm:database:mybatis-auto-wrapper-core")
include(":lib:tool-jvm:network-call:browser:tool-api-browser-automation")
include(":lib:tool-jvm:network-call:browser:ws-automation")
include(":lib:tool-jvm:network-call:tool-api-email")
include(":lib:tool-jvm:tool-bean")
include(":lib:tool-jvm:tool-spctx")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-data-permission")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mybatis")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-rpc")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-security")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-web")
include(":lib:tool-jvm:yudao:yudao-common")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor")
include(":lib:tool-starter:controller-autoconfigure")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Module Sleep: End Of Block <<<

//include(":lib:api:api-openai") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao:yudao-dependencies") // excluded by Gradle Buddy
//include(":lib:tool-jvm:tool-jackson") // excluded by Gradle Buddy
//include(":lib:tool-jvm:tool-jackson-extra") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao:yudao-common")
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
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor")
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-protection") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-rpc") // excluded by Gradle Buddy

//include(":lib:tool-jvm:yudao3:yudao-dependencies") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common")
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-env") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-web")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-security")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mybatis")
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-redis") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mq") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-job") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-tenant") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-data-permission")
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-excel") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-websocket") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-monitor") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-protection") // excluded by Gradle Buddy
//include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-ip") // excluded by Gradle Buddy
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-rpc")
