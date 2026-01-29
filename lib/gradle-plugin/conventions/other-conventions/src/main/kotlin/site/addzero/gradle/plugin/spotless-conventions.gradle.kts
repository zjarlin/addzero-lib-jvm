package site.addzero.gradle.plugin

plugins {
  id("com.diffplug.spotless")
  id("site.addzero.gradle.plugin.repositories-conventions")
}

spotless {
  kotlinGradle {
    ktfmt().googleStyle().configure {
      it.setMaxWidth(160)
      it.setBlockIndent(2)
      it.setContinuationIndent(2)
      it.setRemoveUnusedImports(true)
    }
  }
  if (plugins.hasPlugin("org.jetbrains.kotlin.jvm") ||
    plugins.hasPlugin("org.jetbrains.kotlin.android") ||
    plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
  ) {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/generated/**")
      licenseHeader("")
      ktfmt().googleStyle().configure {
        it.setMaxWidth(160)
        it.setBlockIndent(2)
        it.setContinuationIndent(2)
        it.setRemoveUnusedImports(true)
      }
    }
  }
}
