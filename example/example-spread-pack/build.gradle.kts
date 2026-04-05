import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("site.addzero.kcp.spread-pack")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "site.addzero.example.DesktopMainKt"
        jvmArgs += listOf(
            "-Dsun.java2d.metal=false",
            "-Dfile.encoding=UTF-8",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8",
        )
    }
}

tasks.register<JavaExec>("previewComposeTextSpreadPack") {
    group = "application"
    description = "运行 spread-pack 二次封装 Material3 Text 的桌面示例。"
    dependsOn("classes")
    classpath(sourceSets.main.get().runtimeClasspath)
    mainClass.set("site.addzero.example.DesktopMainKt")
    val autoExitMillis = System.getProperty("spread.pack.desktop.autoExitMillis")
    if (!autoExitMillis.isNullOrBlank()) {
        systemProperty("spread.pack.desktop.autoExitMillis", autoExitMillis)
    }
    jvmArgs(
        "-Dsun.java2d.metal=false",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
    )
}
