plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.com.squareup.javapoet)
    api(libs.site.addzero.lsi.apt)
    api(libs.site.addzero.lsi.core)
    implementation(libs.site.addzero.tool.str)
}
