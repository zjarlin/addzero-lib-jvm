plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    //用apt不要用kapt
    implementation(project(":checkouts:metaprogramming-lsi:lsi-core"))
    implementation(project(":checkouts:metaprogramming-lsi:lsi-apt"))
    api(project(":lib:apt:dict-trans:apt-dict-trans-core"))
    implementation("com.squareup:javapoet:1.13.0")
}

