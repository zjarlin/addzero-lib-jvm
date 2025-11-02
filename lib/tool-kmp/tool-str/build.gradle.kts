plugins {
    id("kmp-core")
}

kotlin{
    sourceSets {
        jvmMain . dependencies {
            implementation("site.addzero:tool-pinyin:+")
            }
        }
    }

