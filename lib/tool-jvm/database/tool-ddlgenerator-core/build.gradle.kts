plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // LSI core
    implementation(project(":checkouts:metaprogramming-lsi:lsi-core"))
    
    // 数据库模型
    implementation(project(":lib:tool-jvm:database:tool-database-model"))
}
