plugins {
    `java`
    application
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":kotlin-lib"))
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("org-junit-platform-junit-platform-launcher").get())
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
