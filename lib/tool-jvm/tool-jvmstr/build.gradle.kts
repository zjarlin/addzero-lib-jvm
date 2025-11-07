plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(projects.lib.toolKmp.toolStr)
    implementation("site.addzero:tool-pinyin:2025.10.07")

}
