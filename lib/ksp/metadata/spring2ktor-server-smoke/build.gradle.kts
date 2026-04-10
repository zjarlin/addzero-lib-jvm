plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    alias(libs.plugins.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    alias(libs.plugins.kotlinSerialization)
}
val catalogLibs = versionCatalogs.named("libs")

val ktorVersion = "3.4.0"

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    implementation(catalogLibs.findLibrary("spring2ktor-server-core").get())
    ksp(project(":lib:ksp:metadata:spring2ktor-server-processor"))

    implementation(catalogLibs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
    implementation(catalogLibs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
    implementation(catalogLibs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")

    compileOnly(catalogLibs.findLibrary("org-springframework-spring-web").get())

    testImplementation("io.ktor:ktor-client-websockets:$ktorVersion")
    testImplementation(catalogLibs.findLibrary("io-ktor-ktor-server-status-pages").get())
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
}
