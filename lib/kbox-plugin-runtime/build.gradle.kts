plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

dependencies {
    implementation(project(":lib:kbox-core"))
    implementation(project(":lib:kbox-plugin-api"))
}

tasks.named<Test>("test") {
    val runtimeFixtureDir = rootProject.file("apps/kbox/runtime-fixtures/hello-plugin")
    if (runtimeFixtureDir.isDirectory) {
        dependsOn(":apps:kbox:runtime-fixtures:hello-plugin:packageRuntimePlugin")
    }
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty(
        "kbox.runtimeFixtureDir",
        runtimeFixtureDir.resolve("build/runtime-plugin/hello-runtime").absolutePath,
    )
}
