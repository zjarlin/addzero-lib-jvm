object BuildSettings {


    object Android {
        const val ANDROID_APP_ID = "compileOptions.addzero.kmp"
        const val BUILD_TYPE = "release"
    }

    object Desktop {
        const val MAIN_CLASS = "${PACKAGE_NAME}.MainKt"
    }

    const val PROJECT_DESCRIPTION = "kmp+jimmer全栈脚手架"
    const val AUTH_NAME = "zjarlin"
    const val PACKAGE_NAME = "site.addzero"
    const val GIT_URL = "https://gitee.com/zjarlin/addzero.git"


    const val KSP_BUILD_DIR_KMP = "build/generated/ksp/metadata/commonMain/kotlin"
    const val KSP_BUILD_DIR_JVM = "build/generated/ksp/main/kotlin"
    const val SOURCE_DIR_KMP = "src/commonMain/kotlin"
    const val SOURCE_DIR_JVM = "src/main/kotlin"
    const val RESOURCE_DIR_JVM = "src/main/resources"
}

val BuildSettings.email: String
    get() = "$AUTH_NAME@outlook.com"
val BuildSettings.gitBaseUrl: String
    get() = GIT_URL.removeSuffix(".git")

val BuildSettings.gitRepoPath: String
    get() = GIT_URL.substringAfter("://").substringAfter("/")

val BuildSettings.gitHost: String
    get() = GIT_URL.substringAfter("://").substringBefore("/")

val BuildSettings.gitRepoName: String
    get() = gitRepoPath.removeSuffix(".git")
