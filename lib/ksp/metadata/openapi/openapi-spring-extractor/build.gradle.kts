plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:ksp:metadata:openapi:openapi-core"))
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)

    testImplementation(libs.org.springframework.spring.context)
    testImplementation(libs.org.springframework.spring.web)
}

description = "Extract Spring controller metadata into in-memory OpenAPI-oriented data classes"
