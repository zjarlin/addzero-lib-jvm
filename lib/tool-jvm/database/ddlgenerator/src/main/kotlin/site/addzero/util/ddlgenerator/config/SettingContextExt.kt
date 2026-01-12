package site.addzero.util.ddlgenerator.config

import org.babyfish.jimmer.config.autoddl.SettingContext
import site.addzero.util.DatabaseConfigReader
import site.addzero.util.KoinInjector.getSupportStrategty
import site.addzero.util.db.DatabaseType
import site.addzero.util.lsi.database.dialect.DdlGenerationStrategy
import site.addzero.util.ddlgenerator.assist.guessDabaseType
import site.addzero.util.ddlgenerator.assist.guessSchema
import site.addzero.util.str.toNotEmptyStr
fun SettingContext.guessFromYml() {
    val cfg = DatabaseConfigReader.fromSpringYml(this.springResourcePath) ?: return
    if (jdbcUrl.isBlank()) jdbcUrl = cfg.jdbcUrl.toNotEmptyStr()
    if (jdbcUsername.isBlank()) jdbcUsername = cfg.jdbcUsername.toNotEmptyStr()
    if (jdbcPassword.isBlank()) jdbcPassword = cfg.jdbcPassword.toNotEmptyStr()
}
val SettingContext.databaseType: DatabaseType
    get() {
        val guessDabaseType = guessDabaseType(this.jdbcUrl)
        return guessDabaseType
    }
val SettingContext.schema: String?
    get() {
        val guessSchema = guessSchema(this.jdbcUrl)
        return guessSchema
    }
val SettingContext.strategy: DdlGenerationStrategy
    get() {
        val supportStrategty = getSupportStrategty<DdlGenerationStrategy> {
            it.support(databaseType)
        }
        return supportStrategty
    }
