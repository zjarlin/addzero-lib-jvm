package site.addzero.gradle

import org.gradle.api.provider.Property

interface AdzeroExtension {
    val jdkVersion: Property<String>
    val springVersion: Property<String>
}

