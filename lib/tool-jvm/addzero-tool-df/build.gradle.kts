plugins {
    id("kotlin-graal-vm")
    id("jvm-json-withtool")
    application
    id("jvm-ksp-plugin")
    id("jvm-koin-core")
}


dependencies {
    implementation(libs.kotlinx.coroutines.core )
    ksp(libs.addzero.ioc.processor)
    implementation(libs.addzero.ioc.core)
    implementation(libs.addzero.tool.io)
    implementation(libs.kotlin.reflect)
}

application {
    mainClass = "site.addzero.app.AppKt"
    applicationName = "dotfiles-cli"
}
