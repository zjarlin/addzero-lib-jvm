package site.addzero.ddlgenerator.runtime

import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions
import site.addzero.ddlgenerator.core.options.AutoDdlOptions
import site.addzero.ddlgenerator.runtime.config.SettingContext
import site.addzero.util.DatabaseConfigReader
import site.addzero.util.db.DatabaseType

fun SettingContext.guessFromYml() {
    if (jdbcUrl.isNotBlank()) {
        return
    }
    val config = DatabaseConfigReader.fromSpringYml(springResourcePath.takeIf { it.isNotBlank() }) ?: return
    if (jdbcUrl.isBlank()) {
        jdbcUrl = config.jdbcUrl.orEmpty()
    }
    if (jdbcUsername.isBlank()) {
        jdbcUsername = config.jdbcUsername.orEmpty()
    }
    if (jdbcPassword.isBlank()) {
        jdbcPassword = config.jdbcPassword.orEmpty()
    }
}

fun SettingContext.databaseType(): DatabaseType {
    val type = DatabaseType.fromUrl(jdbcUrl)
    require(type != null) { "Cannot determine database type from jdbcUrl '$jdbcUrl'" }
    return type
}

fun SettingContext.schema(): String? {
    return when {
        jdbcUrl.startsWith("jdbc:postgresql:") ->
            Regex("[?&]schema=([^&]*)").find(jdbcUrl)?.groupValues?.getOrNull(1)
        jdbcUrl.startsWith("jdbc:mysql:") ->
            jdbcUrl.substringAfter("jdbc:mysql://").substringAfter("/").substringBefore("?").ifBlank { null }
        jdbcUrl.startsWith("jdbc:sqlserver:") ->
            Regex("[?;]databaseName=([^;]*)").find(jdbcUrl)?.groupValues?.getOrNull(1) ?: "dbo"
        jdbcUrl.startsWith("jdbc:h2:") -> "PUBLIC"
        jdbcUrl.startsWith("jdbc:sqlite:") -> "main"
        jdbcUrl.startsWith("jdbc:dm:") ->
            jdbcUrl.substringAfter("//").substringAfter("/").substringBefore("?").ifBlank { null }
        jdbcUrl.startsWith("jdbc:kingbase:") || jdbcUrl.startsWith("jdbc:kingbase8:") ->
            jdbcUrl.substringAfter("//").substringAfter("/").substringBefore("?").ifBlank { null }
        else -> null
    }
}

fun SettingContext.toJdbcConfig(): AutoDdlJdbcConfig {
    return AutoDdlJdbcConfig(
        jdbcUrl = jdbcUrl,
        jdbcUsername = jdbcUsername,
        jdbcPassword = jdbcPassword,
        schema = schema(),
        excludeTables = autoddlExcludeTables.map(String::trim).filter(String::isNotBlank),
    )
}

fun SettingContext.toDiffOptions(): AutoDdlDiffOptions {
    val excludeColumns = autoddlExcludeColumns
        .split(",")
        .map(String::trim)
        .filter(String::isNotBlank)
    return AutoDdlDiffOptions(
        ddlOptions = AutoDdlOptions(
            includeForeignKeys = autoddlForeignKeys,
            includeIndexes = autoddlKeys,
            includeComments = true,
            includeSequences = true,
        ),
        allowDestructiveChanges = autoddlAllowDeleteColumn,
        excludeTables = autoddlExcludeTables.map(String::trim).filter(String::isNotBlank),
        excludeColumns = excludeColumns,
    )
}
