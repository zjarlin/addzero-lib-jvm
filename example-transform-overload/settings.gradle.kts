import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
  includeBuild("../build-logic")
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.transform-overload") version "2026.03.13"
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    mavenLocal()
    mavenCentral()
    google()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }

}

rootProject.name = "example-transform-overload"
