package site.addzero.ksp.generated

import org.gradle.api.provider.Property

// 由 ProcessorBuddy mustMap 固化，请保持与 jimmer-ddl-compiler-processor/build.gradle.kts 同步。
abstract class JimmerDdlCompilerExtension {
    abstract val enabled: Property<Boolean>
    abstract val profiles: Property<String>
    abstract val databaseType: Property<String>
    abstract val outputFormat: Property<String>
    abstract val outputDir: Property<String>
    abstract val version: Property<String>
    abstract val description: Property<String>
    abstract val includePackages: Property<String>
    abstract val excludePackages: Property<String>
    abstract val includeForeignKeys: Property<Boolean>
    abstract val includeIndexes: Property<Boolean>
    abstract val includeComments: Property<Boolean>
    abstract val includeSequences: Property<Boolean>
    abstract val includeManyToManyTables: Property<Boolean>

    init {
        enabled.convention(true)
        profiles.convention("")
        databaseType.convention("postgresql")
        outputFormat.convention("flyway")
        outputDir.convention("build/generated/jimmer-ddl/main/resources/db/migration")
        version.convention("1001")
        description.convention("jimmer_auto_ddl_generated")
        includePackages.convention("")
        excludePackages.convention("")
        includeForeignKeys.convention(true)
        includeIndexes.convention(true)
        includeComments.convention(true)
        includeSequences.convention(true)
        includeManyToManyTables.convention(true)
    }
}

// 由 ProcessorBuddy mustMap 固化，请保持与 jimmer-ddl-compiler-processor/build.gradle.kts 同步。
fun collectJimmerDdlCompilerExtensionKspArgs(
    extension: JimmerDdlCompilerExtension,
    defaultOutputDir: String,
): LinkedHashMap<String, String> =
    linkedMapOf<String, String>().apply {
        put("jimmerDdl.enabled", extension.enabled.get().toString())
        put("jimmerDdl.profiles", extension.profiles.get())
        put("jimmerDdl.databaseType", extension.databaseType.get())
        put("jimmerDdl.outputFormat", extension.outputFormat.get())
        put(
            "jimmerDdl.outputDir",
            extension.outputDir.orNull
                ?.takeIf { configured ->
                    configured.isNotBlank() &&
                        configured != "build/generated/jimmer-ddl/main/resources/db/migration"
                }
                ?: defaultOutputDir,
        )
        put("jimmerDdl.version", extension.version.get())
        put("jimmerDdl.description", extension.description.get())
        put("jimmerDdl.includePackages", extension.includePackages.get())
        put("jimmerDdl.excludePackages", extension.excludePackages.get())
        put("jimmerDdl.includeForeignKeys", extension.includeForeignKeys.get().toString())
        put("jimmerDdl.includeIndexes", extension.includeIndexes.get().toString())
        put("jimmerDdl.includeComments", extension.includeComments.get().toString())
        put("jimmerDdl.includeSequences", extension.includeSequences.get().toString())
        put("jimmerDdl.includeManyToManyTables", extension.includeManyToManyTables.get().toString())
    }
