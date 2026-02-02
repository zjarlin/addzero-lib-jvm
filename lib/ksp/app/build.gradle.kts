plugins {
    kotlin("jvm") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
}

repositories {
    mavenCentral()
}

dependencies {
    // Runtime dependencies might be needed
    implementation(project(":logger-api"))
    implementation(project(":logger-implementation"))

    // Use the ksp configuration to apply our processor
    ksp(project(":logger-processor"))
}
