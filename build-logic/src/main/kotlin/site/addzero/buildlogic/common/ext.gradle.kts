package site.addzero.buildlogic.common

import site.addzero.gradle.AdzeroExtension


val value = extensions.create<AdzeroExtension>("adzeroExtension").apply {
    springVersion.convention("2.7.5")
    jdkVersion.convention("8")
}


