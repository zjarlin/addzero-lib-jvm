plugins {
    id("kotlin-graal-vm")
    id("jvm-json-withtool")
    application
    id("jvm-ksp-plugin")
    id("jvm-koin-core")
}


dependencies {
    implementation(libs.kotlinx.coroutines.core )
    implementation(libs.addzero.tool.io)
    implementation(libs.kotlin.reflect)
    implementation(projects.lib.toolJvm.addzeroToolCliRepl)
}

application {
    mainClass = "site.addzero.app.AppKt"
    applicationName = "dotfiles-cli"
}
