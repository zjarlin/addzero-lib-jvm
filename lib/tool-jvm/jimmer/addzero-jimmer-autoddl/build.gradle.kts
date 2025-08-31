plugins {
    id("kotlin-convention")
}


// 设置兼容的JDK版本
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(8))
//    }
//}
//
//kotlin {
//    jvmToolchain(8)
//}

dependencies {
    implementation(libs.pinyin4j)
    implementation(libs.ksp.symbol.processing.api)
    implementation(libs.hutool.all)


}


