package site.addzero.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class KoinConventionExtension @Inject constructor(objects: ObjectFactory) {

    val koinBomVersion: Property<String> = objects.property(String::class.java)
        .convention("4.2.0-beta2")

    val koinAnnotationsVersion: Property<String> = objects.property(String::class.java)
        .convention("2.3.2-Beta1")

    val toolKoinVersion: Property<String> = objects.property(String::class.java)
        .convention("2025.12.30")

    val kspVersion:Property<String>  = objects.property(String::class.java).convention("2.3.2-Beta1")
}
