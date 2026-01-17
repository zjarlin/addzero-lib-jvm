plugins {
    id("site.addzero.buildlogic.jvm.jvm-json")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

dependencies {
    implementation(libs.okhttp)
    // KSP Singleton Adapter
    implementation(projects.lib.ksp.metadata.singletonAdapterApi)
    ksp(projects.lib.ksp.metadata.singletonAdapterProcessor)

}
