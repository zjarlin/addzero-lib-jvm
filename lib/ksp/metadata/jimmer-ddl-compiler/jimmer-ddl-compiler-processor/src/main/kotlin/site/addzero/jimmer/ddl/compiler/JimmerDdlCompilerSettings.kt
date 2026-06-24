package site.addzero.jimmer.ddl.compiler

import site.addzero.ddlgenerator.core.options.AutoDdlOptions
import site.addzero.util.db.DatabaseType

private const val DEFAULT_OUTPUT_DIR = "build/generated/jimmer-ddl/main/resources/db/migration"
private const val DEFAULT_VERSION = "1001"
private const val DEFAULT_DESCRIPTION = "jimmer_auto_ddl_generated"

enum class JimmerDdlOutputFormat {
    PLAIN,
    FLYWAY;

    companion object {
        fun parse(value: String): JimmerDdlOutputFormat {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: FLYWAY
        }
    }
}

data class JimmerDdlCompilerSettings(
    val enabled: Boolean = true,
    val databaseType: DatabaseType = DatabaseType.POSTGRESQL,
    val outputFormat: JimmerDdlOutputFormat = JimmerDdlOutputFormat.FLYWAY,
    val outputDir: String = DEFAULT_OUTPUT_DIR,
    val version: String = DEFAULT_VERSION,
    val description: String = DEFAULT_DESCRIPTION,
    val includePackages: List<String> = emptyList(),
    val excludePackages: List<String> = emptyList(),
    val options: AutoDdlOptions = AutoDdlOptions(),
    val includeManyToManyTables: Boolean = true,
) {
    fun includesClass(qualifiedName: String?): Boolean {
        if (qualifiedName.isNullOrBlank()) {
            return includePackages.isEmpty()
        }

        if (excludePackages.any { packagePrefix -> qualifiedName.isInPackagePrefix(packagePrefix) }) {
            return false
        }

        if (includePackages.isEmpty()) {
            return true
        }

        return includePackages.any { packagePrefix -> qualifiedName.isInPackagePrefix(packagePrefix) }
    }

    val outputFileName: String
        get() {
            val normalizedDescription = description
                .trim()
                .ifBlank { DEFAULT_DESCRIPTION }
                .replace(Regex("[^A-Za-z0-9_]+"), "_")
                .trim('_')
                .ifBlank { DEFAULT_DESCRIPTION }
            return when (outputFormat) {
                JimmerDdlOutputFormat.FLYWAY -> "V${version.trim().ifBlank { DEFAULT_VERSION }}__${normalizedDescription}.sql"
                JimmerDdlOutputFormat.PLAIN -> "${normalizedDescription}.sql"
            }
        }

    companion object {
        fun allFromOptions(options: Map<String, String>): List<JimmerDdlCompilerSettings> {
            val profileNames = options.option("jimmerDdl.profiles", defaultValue = "").toValueList()
            if (profileNames.isEmpty()) {
                return listOf(fromOptions(options))
            }
            return profileNames.map { profileName ->
                fromProfileOptions(options, profileName)
            }
        }

        fun fromOptions(options: Map<String, String>): JimmerDdlCompilerSettings {
            val outputDir = options.option(
                newKey = "jimmerDdl.outputDir",
                legacyKey = "sqlSavePath",
                defaultValue = DEFAULT_OUTPUT_DIR,
            )
            val databaseType = parseDatabaseType(
                options.option(
                    newKey = "jimmerDdl.databaseType",
                    legacyKey = "dbType",
                    defaultValue = DatabaseType.POSTGRESQL.name,
                )
            )
            return JimmerDdlCompilerSettings(
                enabled = options.option("jimmerDdl.enabled", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                databaseType = databaseType,
                outputFormat = JimmerDdlOutputFormat.parse(options.option("jimmerDdl.outputFormat", defaultValue = "flyway")),
                outputDir = outputDir,
                version = options.option("jimmerDdl.version", defaultValue = DEFAULT_VERSION),
                description = options.option("jimmerDdl.description", defaultValue = DEFAULT_DESCRIPTION),
                includePackages = options.option("jimmerDdl.includePackages", defaultValue = "").toPackageFilters(),
                excludePackages = options.option("jimmerDdl.excludePackages", defaultValue = "").toPackageFilters(),
                options = AutoDdlOptions(
                    includeForeignKeys = options.option("jimmerDdl.includeForeignKeys", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeIndexes = options.option("jimmerDdl.includeIndexes", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeComments = options.option("jimmerDdl.includeComments", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeSequences = options.option("jimmerDdl.includeSequences", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                ),
                includeManyToManyTables = options.option("jimmerDdl.includeManyToManyTables", defaultValue = "true").toBooleanStrictOrNull() ?: true,
            )
        }

        private fun fromProfileOptions(
            options: Map<String, String>,
            profileName: String,
        ): JimmerDdlCompilerSettings {
            val outputDir = options.profileOption(
                profileName = profileName,
                key = "outputDir",
                legacyKey = "sqlSavePath",
                defaultValue = DEFAULT_OUTPUT_DIR,
            )
            val databaseType = parseDatabaseType(
                options.profileOption(
                    profileName = profileName,
                    key = "databaseType",
                    legacyKey = "dbType",
                    defaultValue = DatabaseType.POSTGRESQL.name,
                )
            )
            return JimmerDdlCompilerSettings(
                enabled = options.profileOption(profileName, "enabled", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                databaseType = databaseType,
                outputFormat = JimmerDdlOutputFormat.parse(options.profileOption(profileName, "outputFormat", defaultValue = "flyway")),
                outputDir = outputDir,
                version = options.profileOption(profileName, "version", defaultValue = DEFAULT_VERSION),
                description = options.profileOption(profileName, "description", defaultValue = "${profileName}_generated"),
                includePackages = options.profileOption(profileName, "includePackages", defaultValue = "").toPackageFilters(),
                excludePackages = options.profileOption(profileName, "excludePackages", defaultValue = "").toPackageFilters(),
                options = AutoDdlOptions(
                    includeForeignKeys = options.profileOption(profileName, "includeForeignKeys", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeIndexes = options.profileOption(profileName, "includeIndexes", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeComments = options.profileOption(profileName, "includeComments", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                    includeSequences = options.profileOption(profileName, "includeSequences", defaultValue = "true").toBooleanStrictOrNull() ?: true,
                ),
                includeManyToManyTables = options.profileOption(profileName, "includeManyToManyTables", defaultValue = "true").toBooleanStrictOrNull() ?: true,
            )
        }

        private fun String.toPackageFilters(): List<String> {
            return toValueList()
        }

        private fun String.toValueList(): List<String> {
            return split(',', ';')
                .map { it.trim().trimEnd('.') }
                .filter { it.isNotBlank() }
                .distinct()
        }

        private fun parseDatabaseType(rawValue: String): DatabaseType {
            val normalized = rawValue.trim().lowercase()
            val alias = when (normalized) {
                "pg", "postgres" -> "postgresql"
                "mssql" -> "sqlserver"
                "dameng" -> "dm"
                else -> normalized
            }
            return DatabaseType.fromCode(alias)
                ?: DatabaseType.fromName(alias)
                ?: DatabaseType.POSTGRESQL
        }

        private fun Map<String, String>.option(
            newKey: String,
            legacyKey: String? = null,
            defaultValue: String,
        ): String {
            val newValue = this[newKey]
            if (!newValue.isNullOrBlank()) {
                return newValue
            }
            val legacyValue = legacyKey?.let { this[it] }
            if (!legacyValue.isNullOrBlank()) {
                return legacyValue
            }
            return defaultValue
        }

        private fun Map<String, String>.profileOption(
            profileName: String,
            key: String,
            legacyKey: String? = null,
            defaultValue: String,
        ): String {
            val profileValue = this["jimmerDdl.profile.$profileName.$key"]
            if (!profileValue.isNullOrBlank()) {
                return profileValue
            }
            return option(
                newKey = "jimmerDdl.$key",
                legacyKey = legacyKey,
                defaultValue = defaultValue,
            )
        }
    }
}

private fun String.isInPackagePrefix(packagePrefix: String): Boolean {
    return this == packagePrefix || startsWith("$packagePrefix.")
}
