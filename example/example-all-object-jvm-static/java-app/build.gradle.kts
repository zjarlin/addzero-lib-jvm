plugins {
    `java`
    application
}

dependencies {
    implementation(project(":kotlin-lib"))
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testRuntimeOnly(libs.org.junit.platform.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("site.addzero.example.app.JavaMain")
}
