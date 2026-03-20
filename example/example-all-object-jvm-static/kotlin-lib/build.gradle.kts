plugins {
    alias(libs.plugins.kotlinJvm)
    `java-library`
    id("site.addzero.kcp.all-object-jvm-static")
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
