plugins {
    `java`
    application
}
val libs = versionCatalogs.named("libs")
val kotlinLibProjectPath =
    if (findProject(":example:example-all-object-jvm-static:kotlin-lib") != null) {
        ":example:example-all-object-jvm-static:kotlin-lib"
    } else {
        ":kotlin-lib"
    }

dependencies {
    implementation(project(kotlinLibProjectPath))
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
