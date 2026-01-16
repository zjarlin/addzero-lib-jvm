plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // 依赖音乐搜索模块
    implementation(project(":lib:tool-jvm:network-call:music:tool-api-music-search"))
    // 依赖 Suno API 模块
    implementation(project(":lib:tool-jvm:network-call:music:tool-api-suno"))

}

