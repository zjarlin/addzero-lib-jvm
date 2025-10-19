package site.addzero.util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

inline fun <reified T : Any> Project.createExtension(defaultValue: String=""): T {
    val create = extensions.create<T>(
        T::class.simpleName?.lowercase() ?: defaultValue

    )
    return create
}
