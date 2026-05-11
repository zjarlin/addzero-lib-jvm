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
    implementation(catalogLibs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(catalogLibs.findLibrary("io-insert-koin-koin-annotations").get())
    implementation(catalogLibs.findLibrary("io-insert-koin-koin-core").get())

    implementation(catalogLibs.findLibrary("modbus-runtime").get())
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp"))
    ksp(project(":example:modbus:modbus-ksp-rtu-smoke-provider"))

    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

val smokeMetadataDbFile = layout.buildDirectory.file("modbus-smoke/metadata.sqlite")

ksp {
    arg("addzero.modbus.transports", "rtu")
    arg("addzero.modbus.codegen.mode", "gateway,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
    arg("addzero.modbus.metadata.providers", "smoke-sqlite")
    arg("addzero.modbus.database.driverClass", "org.sqlite.JDBC")
    arg("addzero.modbus.database.jdbcUrl", "jdbc:sqlite:${smokeMetadataDbFile.get().asFile.absolutePath}")
    arg("addzero.modbus.database.query", "select json_payload from modbus_metadata order by id")
    arg("addzero.modbus.database.jsonColumn", "json_payload")
    arg("addzero.modbus.smoke.sqlite.path", smokeMetadataDbFile.get().asFile.absolutePath)
    arg(
        "addzero.modbus.spring.route.outputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
    arg(
        "addzero.modbus.address.lock.path",
        layout.projectDirectory.file("src/main/modbus/device.rtu.addresses.lock").asFile.absolutePath,
    )
}

val smokeKspTasks = tasks.matching { task ->
    task.name == "kspKotlin" || task.name == "kspKotlinJvm"
}

smokeKspTasks.configureEach {
    dependsOn(prepareSmokeExternalProject)
}

tasks.named<Test>("test") {
    dependsOn(smokeKspTasks)
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
