plugins {
    id("kmp-core")
}

kotlin{
    sourceSets {
        jvmMain . dependencies {
            implementation("site.addzero:tool-pinyin:2025.10.07")
            }
        }
    }

