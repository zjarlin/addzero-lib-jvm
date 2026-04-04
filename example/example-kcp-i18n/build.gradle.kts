plugins {
    alias(libs.plugins.kotlinJvm)
    application
    id("site.addzero.kcp.i18n") version "+"
}

i18n {
    resourceBasePath.set("i18n")
    managedLocales.add("en")
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
