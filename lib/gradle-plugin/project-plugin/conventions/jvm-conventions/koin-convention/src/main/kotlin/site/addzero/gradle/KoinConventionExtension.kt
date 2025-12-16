package site.addzero.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class KoinConventionExtension @Inject constructor(objects: ObjectFactory) {

    val koinBomVersion: Property<String> = objects.property(String::class.java)
        .convention("4.0.0")

    val koinAnnotationsVersion: Property<String> = objects.property(String::class.java)
        .convention("1.4.0")

    val toolKoinVersion: Property<String> = objects.property(String::class.java)
        .convention("2025.09.30")
}