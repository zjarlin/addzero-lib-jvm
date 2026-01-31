package site.addzero.gradle

object BuildSettings {
    object Android {
//        const val ANDROID_APP_ID = "compileOptions.addzero.kmp"
      const val ANDROID_APP_ID = "compileOptions.addzero.kmp"
        const val BUILD_TYPE = "release"
    }
    object Desktop {
        const val MAIN_CLASS = "${PACKAGE_NAME}.MainKt"
    }

    const val PACKAGE_NAME = "site.addzero"
}
