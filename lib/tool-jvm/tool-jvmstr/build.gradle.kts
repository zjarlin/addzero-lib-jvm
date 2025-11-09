plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api("site.addzero:tool-str:${libs.versions.addzero.lib.get()}")
    implementation("site.addzero:tool-pinyin:2025.10.07")

}
