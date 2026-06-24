rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-24T10:27:09.203039
// Loaded: 26, Excluded: 0, Total: 26
include(":lib:apt:dict-trans:apt-dict-trans-processor")
include(":lib:gradle-plugin:tool:gradle-script")
include(":lib:gradle-plugin:tool:gradle-script-core")
include(":lib:kotlin-script")
include(":lib:tool-jvm:database:ddlgenerator-core")
include(":lib:tool-jvm:database:ddlgenerator-lsi-adaptor")
include(":lib:tool-jvm:database:mybatis-auto-wrapper")
include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:tool-mybatis")
include(":lib:tool-jvm:tool-bean")
include(":lib:tool-jvm:tool-const")
include(":lib:tool-jvm:tool-excel")
include(":lib:tool-jvm:tool-ip")
include(":lib:tool-jvm:tool-reflection")
include(":lib:tool-jvm:tool-spctx")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-common")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-data-permission")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-ip")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-mybatis")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-rpc")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-security")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-web")
include(":lib:tool-jvm:yudao:yudao-common")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-monitor")
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
include(":lib:tool-jvm:yudao:yudao-spring-boot-starter-biz-ip")
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
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-biz-ip")
include(":lib:tool-jvm:yudao3:yudao-framework:yudao-spring-boot-starter-rpc")
// >>> Jimmer DDL Compiler local modules >>>
fun includeIfMissing(path: String) {
  if (findProject(path) == null) {
    include(path)
  }
}

listOf(
  ":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-processor",
  ":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-gradle-plugin",
  ":lib:lsi:lsi-core",
  ":lib:lsi:lsi-ksp",
  ":lib:lsi:lsi-apt",
  ":lib:lsi:lsi-jimmer",
  ":lib:tool-jvm:database:ddlgenerator",
  ":lib:tool-jvm:database:ddlgenerator-core",
  ":lib:tool-jvm:database:ddlgenerator-lsi-adaptor",
  ":lib:tool-jvm:database:ddlgenerator-jdbc-adaptor",
  ":lib:tool-jvm:database:ddlgenerator-dialect-mysql",
  ":lib:tool-jvm:database:ddlgenerator-dialect-postgresql",
  ":lib:tool-jvm:database:ddlgenerator-dialect-h2",
  ":lib:tool-jvm:database:ddlgenerator-dialect-sqlite",
  ":lib:tool-jvm:database:ddlgenerator-dialect-sqlserver",
  ":lib:tool-jvm:database:ddlgenerator-dialect-oracle",
  ":lib:tool-jvm:database:ddlgenerator-dialect-dm",
  ":lib:tool-jvm:database:ddlgenerator-dialect-kingbase",
  ":lib:tool-jvm:database:ddlgenerator-dialect-taos",
  ":lib:tool-jvm:database:tool-database-model",
  ":lib:tool-jvm:database:tool-sql-executor",
  ":lib:tool-kmp:jdbc:tool-jdbc",
  ":lib:tool-kmp:jdbc:tool-jdbc-model",
).forEach(::includeIfMissing)

includeIfMissing(":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-ksp-smoke")
includeIfMissing(":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-apt-smoke")
// <<< Jimmer DDL Compiler local modules <<<

// >>> Gen Reified published plugin local modules >>>
listOf(
  ":lib:ksp:metadata:gen-reified:gen-reified-core",
  ":lib:ksp:metadata:gen-reified:gen-reified-processor",
  ":lib:ksp:metadata:gen-reified:gen-reified-gradle-plugin",
).forEach(::includeIfMissing)
// <<< Gen Reified published plugin local modules <<<
