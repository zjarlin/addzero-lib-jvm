plugins {
    id("site.addzero.buildlogic.intellij.intellij-platform")
}

dependencies {
    implementation(project(":lib:kcp:transform-overload:kcp-transform-overload-annotations"))
}
