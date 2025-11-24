import me.champeau.gradle.igp.GitIncludeExtension
import org.codehaus.groovy.tools.shell.util.Logger.io

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.repo-buddy") version "+"
//    id("site.addzero.modules-buddy") version "0.0.652"

//    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
    id("me.champeau.includegit") version "+"
}

val bdlogic = "build-logic"
val jvmstable = "addzero-lib-jvm-stable"

//include(":lib:tool-jvm:tool-mybatis-generator")
//include(":lib:tool-jvm:database:tool-cte")
//lib/tool-jvm/database/tool-cte

//autoModules {
//    excludeModules = listOf(bdlogic, jvmstable)
//}
includeBuild("checkouts/$bdlogic")


fun GitIncludeExtension.includeAddzeroProject(projectName: String) {
    include(projectName) {
        uri.set("https://gitee.com/zjarlin/$projectName.git")
        branch.set("master")
    }
}
gitRepositories {
    listOf(bdlogic, jvmstable).forEach {
        includeAddzeroProject(it)
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}


//include(":lib:tool-kmp:tool-expect")
//include(":lib:tool-kmp:tool-coll")
//include(":lib:tool-kmp:tool-json")
include(":lib:tool-kmp:tool-str")
include(":lib:apt-dict-processor")
//include(":lib:tool-kmp:jdbc:tool-jdbc-model")
//include(":lib:tool-kmp:jdbc:tool-jdbc")
//include(":lib:tool-kmp:tool-koin")
//include(":lib:tool-kmp:tool")
//include(":lib:tool-kmp:network-starter")
//include(":lib:ksp:route:route-processor")
//include(":lib:ksp:route:route-core")
//include(":lib:ksp:common:ksp-support-jdbc")
//include(":lib:ksp:common:ksp-support")
//include(":lib:ksp:common:ksp-easycode")
//include(":lib:ksp:common:ksp-easycode-jimmer")
//include(":lib:ksp:jdbc2metadata:jdbc2controller-processor")
//include(":lib:ksp:jdbc2metadata:jdbc2entity-processor")
//include(":lib:ksp:jdbc2metadata:jdbc2enum-processor")
//include(":lib:ksp:metadata:enum-processor")
//include(":lib:ksp:metadata:apiprovider-processor")
//include(":lib:ksp:metadata:controller2iso2dataprovider-processor")
//include(":lib:ksp:metadata:entity2iso-processor")
//include(":lib:ksp:metadata:entity2mcp-processor")
//include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor")
//include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-core")
//include(":lib:ksp:metadata:entity2form:entity2form-core")
//include(":lib:ksp:metadata:entity2form:entity2form-processor")
//include(":lib:ksp:metadata:controller2api-processor")
//include(":lib:ksp:metadata:entity2analysed-support")
//include(":lib:ksp:metadata:ioc:ioc-processor")
//include(":lib:ksp:metadata:ioc:ioc-core")
//include(":lib:compose:shadcn-compose-component")
//include(":lib:gradle-plugin:gradle-tool")
//include(":lib:gradle-plugin:gradle-script")
//include(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
//include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
//include(":lib:gradle-plugin:gradle-script-core")
//include(":lib:gradle-plugin:gradle-tool-config-java")
//include(":lib:gradle-plugin:project-plugin:gradle-version-budy")
//include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
//include(":lib:gradle-plugin:project-plugin:gradle-ksp-buddy")
include(":lib:gradle-plugin:project-plugin:gradle-apt-buddy")
//include(":lib:tool-starter:dict-trans-core")
//include(":lib:tool-starter:controller-advice-spring-boot-starter")
//include(":lib:tool-starter:controller-autoconfigure")
//include(":lib:tool-starter:dict-trans-spring-boot-starter")
//include(":lib:tool-starter:curllog-spring-boot-starter")
include(":lib:tool-jvm:tool-spring")
//include(":lib:tool-jvm:tool-reflection")
//include(":lib:tool-jvm:database:mybatis-auto-wrapper-core")
//include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:tool-mybatis")
include(":lib:tool-jvm:database:tool-cte")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:tool-email")
//include(":lib:tool-jvm:database:tool-ddlgenerator")
//include(":lib:tool-jvm:database:mybatis-auto-wrapper")
//include(":lib:tool-jvm:tool-ai")
//include(":lib:tool-jvm:tool-cli-repl")
//include(":lib:tool-jvm:jimmer:jimmer-ext-lowquery")
//include(":lib:tool-jvm:jimmer:jimmer-ext-dynamic-datasource")
//include(":lib:tool-jvm:jimmer:jimmer-model-lowquery")
//include(":lib:tool-jvm:tool-mybatis-generator")
//include(":lib:tool-jvm:tool-yml")
//include(":lib:tool-jvm:tool-log")
//include(":lib:tool-jvm:tool-mqtt")
//include(":lib:tool-jvm:tool-excel")
//include(":lib:tool-jvm:tool-api-jvm")
include(":lib:tool-jvm:tool-io-codegen")
//include(":lib:tool-jvm:tool-context")
include(":lib:tool-jvm:tool-curl")
//include(":lib:tool-jvm:tool-funbox")
include(":lib:tool-jvm:tool-spel")
include(":lib:tool-jvm:tool-spctx")
//include(":lib:tool-jvm:tool-math")
include(":lib:tool-jvm:network-call:tool-api-maven")
//include(":lib:tool-jvm:network-call:tool-api-tyc")
//include(":lib:tool-jvm:network-call:tool-api-weather")
//include(":lib:tool-jvm:network-call:tool-api-tyc-hw")
//include(":lib:tool-jvm:stream-wrapper")

include(":lib:tool-jvm:tool-jvmstr")
//include(":lib:tool-jvm:tool-common-jvm")
//include(":lib:tool-jvm:tool-pinyin")
//include(":lib:tool-jvm:tool-docker")
//include(":lib:tool-jvm:tool-area")
//include(":lib:tool-jvm:tool-io")
//include(":lib:tool-jvm:tool-jsr")
//include(":lib:tool-jvm:tool-toml")
//include(":lib:tool-jvm:netty-util")
