plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.hutool.all)
    api(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)

}
