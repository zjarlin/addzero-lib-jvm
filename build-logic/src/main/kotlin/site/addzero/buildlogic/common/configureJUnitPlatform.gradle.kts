package site.addzero.buildlogic.common

import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
