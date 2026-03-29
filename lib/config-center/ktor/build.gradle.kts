plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:config-center:spec"))
    implementation(project(":lib:config-center:runtime-jvm"))
    implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
    implementation(libs.findLibrary("spring2ktor-server-core").get())
    compileOnly(libs.findLibrary("org-springframework-spring-web").get())
    testImplementation(libs.findLibrary("ktor-server-test-host").get())
    testImplementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
    testImplementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25))
        },
    )
}
