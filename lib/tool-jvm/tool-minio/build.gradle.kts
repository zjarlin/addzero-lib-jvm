plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(catalogLibs.findLibrary("io-minio-minio").get())
    implementation(catalogLibs.findLibrary("cn-hutool-hutool-core").get())
    implementation(catalogLibs.findLibrary("org-slf4j-slf4j-api").get())

    testImplementation(libs.junit.junit.junit.jupiter.api)
    testRuntimeOnly(libs.junit.junit.junit.jupiter.engine)
}
