plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.javapoet)
    api(libs.lsi.apt)
    api(libs.lsi.core)
    implementation(libs.tool.str)
}
