plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
}

dependencies {
    implementation(project(":lib:ksp:serviceloader-demo:processor"))
    ksp(project(":lib:ksp:serviceloader-demo:processor"))
}