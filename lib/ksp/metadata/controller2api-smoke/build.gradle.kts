import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val controller2ApiGeneratedRoot = layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")
val generatedApiPackage = "sample.api.external.generated"
val generatedApiOutputDir = controller2ApiGeneratedRoot.map { root ->
    root.dir(generatedApiPackage.replace(".", "/"))
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            compileOnly(versionCatalogs.named("libs").findLibrary("org-springframework-spring-web").get())
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
        generatedApiOutputDir.get().asFile.absolutePath,
    )
    // 留空时处理器默认生成同包聚合对象 `Apis`。
    arg("apiClientAggregatorOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
}

tasks.withType<Test>().configureEach {
    dependsOn("kspKotlinJvm")
    systemProperty("controller2api.smoke.projectDir", projectDir.absolutePath)
}

val cleanController2ApiGeneratedRoot by tasks.registering(Delete::class) {
    delete(controller2ApiGeneratedRoot)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    dependsOn(cleanController2ApiGeneratedRoot)
    outputs.dir(controller2ApiGeneratedRoot)
}
