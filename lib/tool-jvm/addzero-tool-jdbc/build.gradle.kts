@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-jvm")
}
kotlin{
    dependencies {
        api(projects.lib.kmp.model.addzeroToolModelJdbc)
    }
}


