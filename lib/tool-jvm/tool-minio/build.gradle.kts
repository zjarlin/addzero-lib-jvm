plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(catalogLibs.findLibrary("io-minio-minio").get())
    implementation(catalogLibs.findLibrary("cn-hutool-hutool-core").get())
    implementation(catalogLibs.findLibrary("org-slf4j-slf4j-api").get())

    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter-api").get())
    testRuntimeOnly(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter-engine").get())
}
