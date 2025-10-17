plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.hutool.all)
    api(projects.lib.toolJvm.jimmer.jimmerModelLowquery)

}
