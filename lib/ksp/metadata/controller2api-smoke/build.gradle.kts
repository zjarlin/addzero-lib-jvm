plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val catalogLibs = versionCatalogs.named("libs")
val controller2ApiGeneratedRoot = layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")
val generatedApiPackage = "sample.api.external.generated"
val generatedBridgePackage = "sample.api.bridge.generated"

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(controller2ApiGeneratedRoot)
            dependencies {
                implementation(catalogLibs.findLibrary("site-addzero-network-starter").get())
            }
        }
        jvmMain.dependencies {
            compileOnly(catalogLibs.findLibrary("org-springframework-spring-web").get())
        }
    }
}

dependencies {
    add("kspJvm", project(":lib:ksp:metadata:controller2api-processor"))
}

ksp {
    arg("apiClientPackageName", generatedApiPackage)
    arg(
        "apiClientOutputDir",
        controller2ApiGeneratedRoot.get().dir(generatedApiPackage.replace(".", "/")).asFile.absolutePath,
    )
    arg("apiClientBridgePackageName", generatedBridgePackage)
    arg(
        "apiClientBridgeOutputDir",
        controller2ApiGeneratedRoot.get().dir(generatedBridgePackage.replace(".", "/")).asFile.absolutePath,
    )
    arg("apiClientBridgeFileName", "Controller2ApiGeneratedClients")
}

tasks.withType<Test>().configureEach {
    dependsOn("kspKotlinJvm")
    systemProperty("controller2api.smoke.projectDir", projectDir.absolutePath)
}
