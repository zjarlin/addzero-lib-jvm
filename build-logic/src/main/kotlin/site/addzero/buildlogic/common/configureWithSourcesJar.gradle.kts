package site.addzero.buildlogic.common

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

extensions.configure<JavaPluginExtension> {
    withSourcesJar()
}
