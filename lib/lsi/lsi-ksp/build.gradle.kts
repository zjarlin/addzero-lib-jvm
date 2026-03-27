plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(project(":lib:lsi:lsi-core"))
    implementation("site.addzero:tool-str:2026.02.23")
    // KSP API dependencies
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())

    // kotlinpoet: LsiClass → ClassName bridge
    compileOnly("com.squareup:kotlinpoet:2.2.0")
    compileOnly("com.squareup:kotlinpoet-ksp:2.2.0")

//    // 测试依赖
//    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-runner-junit5").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-assertions-core").get())
//    testImplementation(libs.findLibrary("io-kotest-kotest-property").get())
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


description = "LSI系统的KSP实现模块"
