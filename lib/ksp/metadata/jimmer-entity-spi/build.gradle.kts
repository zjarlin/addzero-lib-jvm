plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(libs.com.google.devtools.ksp.symbol.processing.api)
    api(libs.androidx.room.compiler.processing)
    api(project(":lib:lsi:lsi-core"))
}
