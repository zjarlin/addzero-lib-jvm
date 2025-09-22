plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation("com.hivemq:hivemq-mqtt-client:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
