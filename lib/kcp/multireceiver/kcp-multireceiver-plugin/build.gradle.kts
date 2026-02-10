plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.site.addzero.addzero.kcp.annotations)
    implementation(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
}
