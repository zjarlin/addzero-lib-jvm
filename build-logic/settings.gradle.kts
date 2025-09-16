rootProject.name = "build-logic"
//includeBuild("lib/gradle-plugin/addzero-gradle-tool")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
