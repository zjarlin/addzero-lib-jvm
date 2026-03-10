plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    alias(libs.plugins.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    implementation(project(":lib:ksp:metadata:spring2ktor-server-core"))
    ksp(project(":lib:ksp:metadata:spring2ktor-server-processor"))

    implementation(libs.io.ktor.ktor.server.content.negotiation)
    implementation(libs.io.ktor.ktor.server.websockets)
    implementation(libs.io.ktor.ktor.serialization.kotlinx.json)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)

    compileOnly(libs.org.springframework.spring.context)
    compileOnly(libs.org.springframework.spring.web)

    testImplementation(libs.io.ktor.ktor.client.websockets)
    testImplementation(libs.io.ktor.ktor.server.test.host.jvm)
}
