package site.addzero.kcp.i18n.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class I18NGradleExtension @Inject constructor(
    objects: ObjectFactory,
) {
    val targetLocale: Property<String> = objects.property(String::class.java)
    val resourceBasePath: Property<String> = objects.property(String::class.java).convention("i18n")
    val managedLocales: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
}
