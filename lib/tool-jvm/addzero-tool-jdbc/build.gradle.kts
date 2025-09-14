plugins {
    id("kmp-jvm")
}
kotlin{
    dependencies {
        api(projects.lib.kmp.model.addzeroToolModelJdbc)
    }
}


