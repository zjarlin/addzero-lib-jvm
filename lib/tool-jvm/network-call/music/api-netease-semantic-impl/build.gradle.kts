plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi"))
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-impl"))
            // 依赖 api-netease 以获取 MusicSearchType 等枚举
            implementation(project(":lib:tool-jvm:network-call:music:api-netease"))
            implementation(kotlin("stdlib"))
        }
    }
}
