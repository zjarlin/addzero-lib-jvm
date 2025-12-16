package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class KspConventionExtension {
    abstract val kspVersion: Property<String>
    abstract val enableKsp: Property<Boolean>

    init {
        kspVersion.convention("2.2.21-2.0.4")
        enableKsp.convention(true)
    }
}