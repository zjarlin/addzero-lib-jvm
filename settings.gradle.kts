rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.repo-buddy") version "+"
    id("site.addzero.modules-buddy") version "+"
    id("me.champeau.includegit") version "+"
}

gitRepositories {
    include("build-logic") {
        uri.set("https://gitee.com/zjarlin/build-logic.git")
        branch.set("master")
    }
    include("addzero-lib-jvm-stable") {
        uri.set("https://gitee.com/zjarlin/addzero-lib-jvm-stable.git")
        branch.set("master")
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}
