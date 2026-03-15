plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project.dependencies.platform(libs.io.insert.koin.koin.bom))
    api(libs.io.insert.koin.koin.core)

    api(libs.io.ktor.ktor.server.core)
    implementation(libs.org.springframework.spring.web)
}
