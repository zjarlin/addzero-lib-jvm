package site.addzero.gradle

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.get


interface DomainConventionExtension {
    val domain: Property<String>
}





