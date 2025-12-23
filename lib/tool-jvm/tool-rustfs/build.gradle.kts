plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    val awsSdkVersion = "2.29.16"

    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
