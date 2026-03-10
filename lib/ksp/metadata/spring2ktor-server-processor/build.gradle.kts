plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)
    testImplementation(libs.org.springframework.spring.context)
    testImplementation(libs.org.springframework.spring.web)
}
