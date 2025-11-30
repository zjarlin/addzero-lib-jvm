import me.champeau.gradle.igp.GitIncludeExtension
import org.codehaus.groovy.tools.shell.util.Logger.io

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.modules-buddy") version "0.0.652"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
//    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
    id("me.champeau.includegit") version "+"
}

val bdlogic = "build-logic"
val jvmstable = "addzero-lib-jvm-stable"

//includeGitProject(":lib:tool-jvm:tool-mybatis-generator")
//includeGitProject(":lib:tool-jvm:database:tool-cte")
//lib/tool-jvm/database/tool-cte

//autoModules {
//    excludeModules = listOf(bdlogic, jvmstable)
//}
includeBuild("checkouts/$bdlogic")
//includeBuild("lib/gradle-plugin/project-plugin/gradle-apt-buddy")



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


//includeGitProject(":lib:tool-kmp:tool-expect")
//includeGitProject(":lib:tool-kmp:tool-coll")
//includeGitProject(":lib:tool-kmp:tool-json")
include(":lib:tool-kmp:tool-str")
include(":lib:apt-dict-processor")
//includeGitProject(":lib:tool-kmp:jdbc:tool-jdbc-model")
//includeGitProject(":lib:tool-kmp:jdbc:tool-jdbc")
//includeGitProject(":lib:tool-kmp:tool-koin")
//includeGitProject(":lib:tool-kmp:tool")
//includeGitProject(":lib:tool-kmp:network-starter")
//includeGitProject(":lib:ksp:route:route-processor")
//includeGitProject(":lib:ksp:route:route-core")
//includeGitProject(":lib:ksp:common:ksp-support-jdbc")
//includeGitProject(":lib:ksp:common:ksp-support")
//includeGitProject(":lib:ksp:common:ksp-easycode")
//includeGitProject(":lib:ksp:common:ksp-easycode-jimmer")
//includeGitProject(":lib:ksp:jdbc2metadata:jdbc2controller-processor")
//includeGitProject(":lib:ksp:jdbc2metadata:jdbc2entity-processor")
//includeGitProject(":lib:ksp:jdbc2metadata:jdbc2enum-processor")
//includeGitProject(":lib:ksp:metadata:enum-processor")
//includeGitProject(":lib:ksp:metadata:apiprovider-processor")
//includeGitProject(":lib:ksp:metadata:controller2iso2dataprovider-processor")
//includeGitProject(":lib:ksp:metadata:entity2iso-processor")
//includeGitProject(":lib:ksp:metadata:entity2mcp-processor")
//includeGitProject(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor")
//includeGitProject(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-core")
//includeGitProject(":lib:ksp:metadata:entity2form:entity2form-core")
//includeGitProject(":lib:ksp:metadata:entity2form:entity2form-processor")
//includeGitProject(":lib:ksp:metadata:controller2api-processor")
//includeGitProject(":lib:ksp:metadata:entity2analysed-support")
//includeGitProject(":lib:ksp:metadata:ioc:ioc-processor")
//includeGitProject(":lib:ksp:metadata:ioc:ioc-core")
//includeGitProject(":lib:compose:shadcn-compose-component")
//includeGitProject(":lib:gradle-plugin:gradle-tool")
//includeGitProject(":lib:gradle-plugin:gradle-script")
//includeGitProject(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")

//includeGitProject(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
//includeGitProject(":lib:gradle-plugin:gradle-script-core")
//includeGitProject(":lib:gradle-plugin:gradle-tool-config-java")
//includeGitProject(":lib:gradle-plugin:project-plugin:gradle-version-budy")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
//includeGitProject(":lib:gradle-plugin:project-plugin:gradle-ksp-buddy")

//includeGitProject(":lib:gradle-plugin:project-plugin:gradle-apt-buddy")
//includeGitProject(":lib:tool-starter:dict-trans-core")
//includeGitProject(":lib:tool-starter:controller-advice-spring-boot-starter")
//includeGitProject(":lib:tool-starter:controller-autoconfigure")
//includeGitProject(":lib:tool-starter:dict-trans-spring-boot-starter")
//includeGitProject(":lib:tool-starter:curllog-spring-boot-starter")
//includeGitProject(":lib:tool-jvm:tool-spring")
//includeGitProject(":lib:tool-jvm:tool-reflection")
//includeGitProject(":lib:tool-jvm:database:mybatis-auto-wrapper-core")
//includeGitProject(":lib:tool-jvm:database:tool-database-model")
//includeGitProject(":lib:tool-jvm:database:tool-mybatis")
//includeGitProject(":lib:tool-jvm:database:tool-cte")
//includeGitProject(":lib:tool-jvm:database:tool-sql-executor")
//includeGitProject(":lib:tool-jvm:tool-email")
//includeGitProject(":lib:tool-jvm:database:tool-ddlgenerator")
//includeGitProject(":lib:tool-jvm:database:mybatis-auto-wrapper")
//includeGitProject(":lib:tool-jvm:tool-ai")
//includeGitProject(":lib:tool-jvm:tool-cli-repl")
//includeGitProject(":lib:tool-jvm:jimmer:jimmer-ext-lowquery")
//includeGitProject(":lib:tool-jvm:jimmer:jimmer-ext-dynamic-datasource")
//includeGitProject(":lib:tool-jvm:jimmer:jimmer-model-lowquery")
//includeGitProject(":lib:tool-jvm:tool-mybatis-generator")
//includeGitProject(":lib:tool-jvm:tool-yml")
//includeGitProject(":lib:tool-jvm:tool-log")
//includeGitProject(":lib:tool-jvm:tool-mqtt")
//includeGitProject(":lib:tool-jvm:tool-excel")
//includeGitProject(":lib:tool-jvm:tool-api-jvm")
//includeGitProject(":lib:tool-jvm:tool-io-codegen")
//includeGitProject(":lib:tool-jvm:tool-context")
//includeGitProject(":lib:tool-jvm:tool-curl")
//includeGitProject(":lib:tool-jvm:tool-funbox")
//includeGitProject(":lib:tool-jvm:tool-spel")
//includeGitProject(":lib:tool-jvm:tool-spctx")
//includeGitProject(":lib:tool-jvm:tool-math")
//includeGitProject(":lib:tool-jvm:network-call:tool-api-maven")
//includeGitProject(":lib:tool-jvm:network-call:tool-api-tyc")
//includeGitProject(":lib:tool-jvm:network-call:tool-api-weather")
//includeGitProject(":lib:tool-jvm:network-call:tool-api-tyc-hw")
//includeGitProject(":lib:tool-jvm:stream-wrapper")

//includeGitProject(":lib:tool-jvm:tool-jvmstr")
//includeGitProject(":lib:tool-jvm:tool-common-jvm")
//includeGitProject(":lib:tool-jvm:tool-pinyin")
//includeGitProject(":lib:tool-jvm:tool-docker")
//includeGitProject(":lib:tool-jvm:tool-area")
//includeGitProject(":lib:tool-jvm:tool-io")
//includeGitProject(":lib:tool-jvm:tool-jsr")
//includeGitProject(":lib:tool-jvm:tool-toml")
//includeGitProject(":lib:tool-jvm:netty-util")
