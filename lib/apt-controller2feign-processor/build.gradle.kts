plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation(project(":checkouts:metaprogramming-lsi:lsi-apt"))
    implementation(project(":checkouts:metaprogramming-lsi:lsi-core"))
    implementation(libs.tool.str)
}
