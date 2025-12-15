plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    //用apt不要用kapt
     implementation(project(":checkouts:metaprogramming-lsi:lsi-core"))
     implementation(project(":checkouts:metaprogramming-lsi:lsi-apt"))
     api(project(":lib:apt:dict-trans:apt-dict-trans-core"))
    implementation("com.squareup:javapoet:1.13.0")

    // 基本APT依赖
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
}

