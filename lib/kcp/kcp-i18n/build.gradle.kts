plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.addzero.kcp.annotations)
    implementation(libs.kotlin.compiler.embeddable)
}


