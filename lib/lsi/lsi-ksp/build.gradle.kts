plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())
    // KSP API dependencies
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())

    // kotlinpoet: LsiClass → ClassName bridge
    compileOnly(libs.findLibrary("com-squareup-kotlinpoet").get())
    compileOnly(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())

//    // 测试依赖
//    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-runner-junit5").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-assertions-core").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-property").get())
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


description = "LSI系统的KSP实现模块"
