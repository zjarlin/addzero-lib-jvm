plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}
dependencies {
    implementation("com.taosdata.jdbc:taos-jdbcdriver:3.0.0")
    implementation(libs.hutool.core)
    implementation("cglib:cglib:3.3.0")
}
