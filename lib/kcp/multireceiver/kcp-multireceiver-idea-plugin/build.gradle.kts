plugins {
    id("site.addzero.buildlogic.intellij.intellij-platform")
}

group = "site.addzero"

dependencies {
    implementation(project(":lib:kcp:multireceiver:kcp-multireceiver-annotations"))
    testImplementation(libs.junit.junit)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}
