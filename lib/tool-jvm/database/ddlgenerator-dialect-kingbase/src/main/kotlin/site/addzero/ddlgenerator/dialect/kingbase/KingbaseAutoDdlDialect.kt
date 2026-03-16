package site.addzero.ddlgenerator.dialect.kingbase

import site.addzero.ddlgenerator.dialect.postgresql.PostgreSqlAutoDdlDialect
import site.addzero.util.db.DatabaseType

class KingbaseAutoDdlDialect : PostgreSqlAutoDdlDialect() {
    override val databaseType: DatabaseType
        get() = DatabaseType.KINGBASE
}
