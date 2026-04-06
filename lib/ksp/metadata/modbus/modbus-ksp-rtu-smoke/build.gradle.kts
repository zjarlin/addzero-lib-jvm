import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    alias(libs.plugins.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    alias(libs.plugins.kotlinSerialization)
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    implementation(catalogLibs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(catalogLibs.findLibrary("io-insert-koin-koin-annotations").get())
    implementation(catalogLibs.findLibrary("io-insert-koin-koin-core").get())

    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))

    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

ksp {
    arg("addzero.modbus.transports", "rtu")
    arg("addzero.modbus.codegen.mode", "gateway,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
    arg(
        "addzero.modbus.spring.route.outputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
    arg("addzero.modbus.address.lock.path", "/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/ksp/metadata/modbus/modbus-ksp-rtu-smoke/src/main/modbus/device.rtu.addresses.lock")
    arg("addzero.modbus.c.output.projectDir", "/Users/zjarlin/IdeaProjects/t")
    arg("addzero.modbus.c.bridgeImpl.path", "Core/Src/modbus")
    arg("addzero.modbus.keil.uvprojx.path", "MDK-ARM/test1.uvprojx")
    arg("addzero.modbus.keil.targetName", "test1")
    arg("addzero.modbus.keil.groupName", "Core/modbus/rtu")
    arg("addzero.modbus.mxproject.path", ".mxproject")
}

tasks.named<Test>("test") {
    dependsOn("kspKotlin")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("modbus.smoke.projectDir", projectDir.absolutePath)
    systemProperty("modbus.smoke.externalProjectDir", "/Users/zjarlin/IdeaProjects/t")
    systemProperty("modbus.smoke.externalBridgeImplPath", "Core/Src/modbus/rtu/device/device_bridge_impl.c")
    systemProperty("modbus.smoke.keilUvprojxPath", "/Users/zjarlin/IdeaProjects/t/MDK-ARM/test1.uvprojx")
    systemProperty("modbus.smoke.mxprojectPath", "/Users/zjarlin/IdeaProjects/t/.mxproject")
    systemProperty("modbus.smoke.addressLockPath", "/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/ksp/metadata/modbus/modbus-ksp-rtu-smoke/src/main/modbus/device.rtu.addresses.lock")
    systemProperty(
        "modbus.smoke.springRouteOutputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
}
