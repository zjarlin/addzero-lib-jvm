plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:ksp:metadata:openapi:openapi-core"))
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)

    testImplementation(libs.io.ktor.ktor.server.core)
    testImplementation(libs.io.ktor.ktor.server.websockets)
}

description = "Extract Ktor route metadata into in-memory OpenAPI-oriented data classes"
