@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

kotlin {
    jvm()
    wasmJs {
        browser()
    }
}


