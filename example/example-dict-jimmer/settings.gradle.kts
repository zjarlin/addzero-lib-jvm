pluginManagement {
    includeBuild("../../checkouts/build-logic")

    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("site\\.addzero(\\..+)?")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("site\\.addzero(\\..+)?")
            }
        }
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "example-dict-jimmer"

include(":app")

include(":lib")
include(":lib:apt")
include(":lib:apt:dict-trans")
include(":lib:tool-jvm")
include(":lib:tool-starter")
project(":lib").projectDir = file("../../lib")
project(":lib:apt").projectDir = file("../../lib/apt")
project(":lib:apt:dict-trans").projectDir = file("../../lib/apt/dict-trans")
project(":lib:tool-jvm").projectDir = file("../../lib/tool-jvm")
project(":lib:tool-starter").projectDir = file("../../lib/tool-starter")

include(":lib:apt:dict-trans:apt-dict-trans-core")
project(":lib:apt:dict-trans:apt-dict-trans-core").projectDir =
    file("../../lib/apt/dict-trans/apt-dict-trans-core")

include(":lib:apt:dict-trans:dict-trans-core")
project(":lib:apt:dict-trans:dict-trans-core").projectDir =
    file("../../lib/apt/dict-trans/dict-trans-core")

include(":lib:tool-jvm:tool-reflection")
project(":lib:tool-jvm:tool-reflection").projectDir = file("../../lib/tool-jvm/tool-reflection")

include(":lib:tool-jvm:tool-bean")
project(":lib:tool-jvm:tool-bean").projectDir = file("../../lib/tool-jvm/tool-bean")

include(":lib:tool-starter:dict-trans-spring-boot-starter")
project(":lib:tool-starter:dict-trans-spring-boot-starter").projectDir =
    file("../../lib/tool-starter/dict-trans-spring-boot-starter")
