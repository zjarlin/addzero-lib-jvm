plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.org.babyfish.jimmer.jimmer.sql.kotlin)
    implementation(libs.cn.hutool.hutool.all)
    api(libs.site.addzero.jimmer.model.lowquery.v2025)

}
