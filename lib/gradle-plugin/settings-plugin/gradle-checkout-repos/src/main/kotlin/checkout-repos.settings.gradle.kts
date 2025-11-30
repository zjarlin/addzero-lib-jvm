import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.gitRepositories

plugins {
    id("me.champeau.includegit")
}

fun GitIncludeExtension.includeAddzeroProject(projectName: String) {
    include(projectName) {
        uri.set("https://gitee.com/zjarlin/$projectName.git")
        branch.set("master")
    }
}

gitRepositories {
    listOf("build-logic", "metaprogramming-lsi", "compose-component").forEach {
        includeAddzeroProject(it)
    }
}

val enableZlibs: String? by settings
if (enableZlibs?.toBoolean() == true) {
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs") {
                from(files("./checkouts/build-logic/gradle/libs.versions.toml"))
            }
        }
    }
}
