package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class KtxJsonConventionExtension {
    abstract val version: Property<String>
    abstract val toolVersion: Property<String>
    init {
        version.convention( "1.9.0")
        toolVersion.convention("2025.09.30")
    }
}