package site.addzero.autoddlstarter.generator

import site.addzero.autoddlstarter.generator.consts.DbType.DM
import site.addzero.autoddlstarter.generator.consts.DbType.H2
import site.addzero.autoddlstarter.generator.consts.DbType.MYSQL
import site.addzero.autoddlstarter.generator.consts.DbType.ORACLE
import site.addzero.autoddlstarter.generator.consts.DbType.POSTGRESQL
import site.addzero.autoddlstarter.generator.ex.*


interface IDatabaseGenerator {
    companion object {

        fun getDatabaseDDLGenerator(dbType: String): DatabaseDDLGenerator {
            val generator = databaseType[dbType]
            return generator!!
        }


        var databaseType: HashMap<String, DatabaseDDLGenerator> = object : HashMap<String, DatabaseDDLGenerator>() {
            init {
                put(MYSQL, MysqlDDLGenerator())
                put(ORACLE, OracleDDLGenerator())
                put(POSTGRESQL, PostgreSQLDDLGenerator())
                put(DM, DMSQLDDLGenerator())
                put(H2, H2SQLDDLGenerator())
            }
        }

    }
}
