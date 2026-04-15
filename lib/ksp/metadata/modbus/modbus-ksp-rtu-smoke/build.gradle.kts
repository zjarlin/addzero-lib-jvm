import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.Sync

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    alias(libs.plugins.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    alias(libs.plugins.kotlinSerialization)
}

val catalogLibs = versionCatalogs.named("libs")
val smokeExternalProjectDir = layout.buildDirectory.dir("smoke/external-project")
val smokeExternalProjectTemplateDir = layout.projectDirectory.dir("src/testFixtures/external-project-template")

val prepareSmokeExternalProject by
    tasks.registering(Sync::class) {
        from(smokeExternalProjectTemplateDir)
        into(smokeExternalProjectDir)
    }

dependencies {
    implementation(catalogLibs.findLibrary("modbus-runtime").get())
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
    arg(
        "addzero.modbus.address.lock.path",
        layout.projectDirectory.file("src/main/modbus/device.rtu.addresses.lock").asFile.absolutePath,
    )
    arg("addzero.modbus.c.output.projectDir", smokeExternalProjectDir.get().asFile.absolutePath)
    arg("addzero.modbus.c.bridgeImpl.path", "Core/Src/modbus")
    arg("addzero.modbus.keil.uvprojx.path", "MDK-ARM/test1.uvprojx")
    arg("addzero.modbus.keil.targetName", "test1")
    arg("addzero.modbus.keil.groupName", "Core/modbus/rtu")
    arg("addzero.modbus.mxproject.path", ".mxproject")
}

tasks.named("kspKotlin") {
    dependsOn(prepareSmokeExternalProject)
}

tasks.named<Test>("test") {
    dependsOn("kspKotlin")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("modbus.smoke.projectDir", projectDir.absolutePath)
    systemProperty("modbus.smoke.externalProjectDir", smokeExternalProjectDir.get().asFile.absolutePath)
    systemProperty("modbus.smoke.externalBridgeImplPath", "Core/Src/modbus/rtu/device/device_bridge_impl.c")
    systemProperty(
        "modbus.smoke.keilUvprojxPath",
        smokeExternalProjectDir.get().asFile.resolve("MDK-ARM/test1.uvprojx").absolutePath,
    )
    systemProperty(
        "modbus.smoke.mxprojectPath",
        smokeExternalProjectDir.get().asFile.resolve(".mxproject").absolutePath,
    )
    systemProperty(
        "modbus.smoke.addressLockPath",
        layout.projectDirectory.file("src/main/modbus/device.rtu.addresses.lock").asFile.absolutePath,
    )
    systemProperty(
        "modbus.smoke.springRouteOutputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
}
