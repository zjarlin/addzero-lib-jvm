plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    api(project(":checkouts:metaprogramming-lsi:lsi-apt"))
    api(project(":checkouts:metaprogramming-lsi:lsi-core"))
    implementation(libs.tool.str)
}
