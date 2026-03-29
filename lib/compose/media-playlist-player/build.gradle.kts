plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-coil")
}

if (providers.gradleProperty("mediaPlaylistPlayer.enableIos").orNull == "true") {
    apply(plugin = "site.addzero.buildlogic.kmp.cmp-ios")
}

val javaFxVersion = "19"
val javaFxClassifier = run {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    when {
        osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm64")) -> "mac-aarch64"
        osName.contains("mac") -> "mac"
        osName.contains("win") -> "win"
        osArch.contains("aarch64") || osArch.contains("arm64") -> "linux-aarch64"
        else -> "linux"
    }
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
        }

        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
        }

        jvmMain.dependencies {
            implementation("org.openjfx:javafx-base:$javaFxVersion:$javaFxClassifier")
            implementation("org.openjfx:javafx-graphics:$javaFxVersion:$javaFxClassifier")
            implementation("org.openjfx:javafx-media:$javaFxVersion:$javaFxClassifier")
        }
    }
}
