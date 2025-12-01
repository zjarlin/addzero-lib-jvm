plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.hierynomus:sshj:0.39.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
}
