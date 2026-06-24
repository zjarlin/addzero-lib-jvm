package site.addzero.ksp.generated

import org.gradle.api.provider.Property

// 由 ProcessorBuddy 生成，请勿手改。
abstract class JimmerLowQueryExtension {
    abstract val generatedPackage: Property<String>

    init {
        generatedPackage.convention("")
    }
}

// 由 ProcessorBuddy 生成，请勿手改。
fun collectJimmerLowQueryExtensionKspArgs(
    extension: JimmerLowQueryExtension,
): LinkedHashMap<String, String> =
    linkedMapOf<String, String>().apply {
        extension.generatedPackage.orNull
            ?.takeIf(String::isNotBlank)
            ?.let { put("jimmerLowQuery.generatedPackage", it) }
    }
