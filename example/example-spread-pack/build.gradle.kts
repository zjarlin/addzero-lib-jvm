plugins {
    alias(libs.plugins.kotlinJvm)
    application
    id("site.addzero.kcp.spread-pack")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("site.addzero.example.MainKt")
}
