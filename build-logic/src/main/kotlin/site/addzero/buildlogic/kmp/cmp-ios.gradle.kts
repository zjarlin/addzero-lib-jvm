package site.addzero.buildlogic.kmp

plugins {
  id("site.addzero.buildlogic.kmp.kmp-convention")
}
kotlin {
  listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

}
