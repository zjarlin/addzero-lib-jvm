package site.addzero.ddlgenerator.runtime

import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions
import site.addzero.ddlgenerator.core.options.AutoDdlOptions
import site.addzero.ddlgenerator.runtime.config.SettingContext
import site.addzero.ddlgenerator.runtime.config.Settings
import site.addzero.lsi.clazz.LsiClass
import site.addzero.util.db.DatabaseType

fun List<LsiClass>.toCompleteSchemaDdl(
    databaseType: DatabaseType,
    options: AutoDdlOptions = AutoDdlOptions(),
    includeManyToManyTables: Boolean = true,
): String {
    return AutoDdlRuntime.generate(this, databaseType, options, includeManyToManyTables).joinToString("\n")
}

fun List<LsiClass>.toDiffDdl(
    actualSchema: AutoDdlSchema,
    databaseType: DatabaseType,
    options: AutoDdlDiffOptions = AutoDdlDiffOptions(),
    includeManyToManyTables: Boolean = true,
): String {
    return AutoDdlRuntime.diff(this, actualSchema, databaseType, options, includeManyToManyTables).toSql()
}

fun List<LsiClass>.toDiffDdl(
    jdbcConfig: AutoDdlJdbcConfig,
    databaseType: DatabaseType,
    options: AutoDdlDiffOptions = AutoDdlDiffOptions(),
    includeManyToManyTables: Boolean = true,
): String {
    return AutoDdlRuntime.diff(this, jdbcConfig, databaseType, options, includeManyToManyTables).toSql()
}

fun List<LsiClass>.toConfiguredDiffDdl(
    settingContext: SettingContext = Settings,
    includeManyToManyTables: Boolean = true,
): String {
    settingContext.guessFromYml()
    return toDiffDdl(
        jdbcConfig = settingContext.toJdbcConfig(),
        databaseType = settingContext.databaseType(),
        options = settingContext.toDiffOptions(),
        includeManyToManyTables = includeManyToManyTables,
    )
}
