plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // 使用project依赖来测试
    implementation(project(":lib:ksp:serviceloader-demo:processor"))
    ksp(project(":lib:ksp:serviceloader-demo:processor"))
}