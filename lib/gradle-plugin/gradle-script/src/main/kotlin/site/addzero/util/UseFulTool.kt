package site.addzero.util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

inline fun<reified T:Any> Project.createExtension(): T {
        return extensions.create<T>(
            T::class.simpleName?.lowercase() ?: "site.addzero"
        )
}
