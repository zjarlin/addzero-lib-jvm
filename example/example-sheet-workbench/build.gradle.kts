import org.gradle.api.tasks.JavaExec

val useIncludedBuild =
    System.getenv("ADDZERO_USE_INCLUDED_BUILD")
        ?.toBooleanStrictOrNull()
        ?: true

fun readRepoVersion(): String {
    val gradlePropertiesFile = file("../../gradle.properties")
    if (!gradlePropertiesFile.isFile) {
        return "2026.10330.12238"
    }
    return gradlePropertiesFile
        .readLines()
        .firstOrNull { line -> line.startsWith("version=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: "2026.10330.12238"
}

val addzeroVersion = readRepoVersion()

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(
        if (useIncludedBuild) {
            "site.addzero:compose-sheet-spi"
        } else {
            "site.addzero:compose-sheet-spi:$addzeroVersion"
        },
    )
    implementation(
        if (useIncludedBuild) {
            "site.addzero:compose-native-component-sheet"
        } else {
            "site.addzero:compose-native-component-sheet:$addzeroVersion"
        },
    )
}

compose.desktop {
    application {
        mainClass = "site.addzero.example.sheet.MainKt"
        jvmArgs += listOf(
            "-Dsun.java2d.metal=false",
            "-Dfile.encoding=UTF-8",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8",
        )
    }
}

tasks.register<JavaExec>("runEngineScenario") {
    group = "application"
    description = "运行在线表格引擎独立场景。"
    dependsOn("classes")
    classpath(sourceSets.main.get().runtimeClasspath)
    mainClass.set("site.addzero.example.sheet.MainKt")
    args("engine")
    jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
    )
}

tasks.register<JavaExec>("previewSheetWorkbench") {
    group = "application"
    description = "运行在线表格桌面工作台。"
    dependsOn("classes")
    classpath(sourceSets.main.get().runtimeClasspath)
    mainClass.set("site.addzero.example.sheet.MainKt")
    val autoExitMillis = System.getProperty("sheet.preview.autoExitMillis")
    if (!autoExitMillis.isNullOrBlank()) {
        systemProperty("sheet.preview.autoExitMillis", autoExitMillis)
    }
    jvmArgs(
        "-Dsun.java2d.metal=false",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
    )
}
