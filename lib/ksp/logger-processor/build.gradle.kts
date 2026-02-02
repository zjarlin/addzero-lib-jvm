plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
}

repositories {
    mavenCentral()
}

dependencies {

    // Depend on the API module so the processor knows about the Logger interface
    implementation(project(":logger-api"))

    // **CRITICAL**: Add the implementation to the processor's classpath
    // so ServiceLoader can find it at compile time.
    implementation(project(":logger-implementation"))
}
