package site.addzero.util.ddlgenerator.diff.comparator

import org.babyfish.jimmer.config.autoddl.Settings
import site.addzero.util.DatabaseMetadataReader
import site.addzero.util.ddlgenerator.config.strategy
import site.addzero.util.ddlgenerator.metadata.databaseMetadataReader
import site.addzero.util.ddlgenerator.metadata.getJdbcMetadata
import site.addzero.util.getIndexMetadata
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.guessTableName
import site.addzero.util.lsi_impl.impl.database.clazz.getDatabaseForeignKeys
import site.addzero.util.lsi_impl.impl.database.clazz.getIndexDefinitions


/**
 * 需要添加的表
 */
fun List<LsiClass>.diffTable(): List<String> =
    getJdbcMetadata()
        .let { metadata ->
            filter { cls -> metadata.none { it.tableName == cls.name } }
                .map { Settings.strategy.generateCreateTable(it) }
        }

/**
 * 需要添加的列
 */
fun List<LsiClass>.diffColumn(): List<String> =
    getJdbcMetadata()
        .let { metadata ->
            flatMap { cls ->
                metadata.find { it.tableName == cls.name }
                    ?.let { dbTable ->
                        cls.fields
                            .filter { field -> dbTable.columns.none { it.columnName == (field.columnName ?: field.name) } }
                            .map { field -> Settings.strategy.generateAddColumn(cls.guessTableName, field) }
                    } ?: emptyList()
            }
        }

/**
 * 需要添加的唯一索引
 * 比对代码中的索引定义与数据库中的索引，找出需要添加的索引
 */
fun List<LsiClass>.diffUniqueIndexes(): List<String> {
    val metadata = getJdbcMetadata()
    val strategy = Settings.strategy

    return flatMap { cls ->
        val tableName = cls.guessTableName
        val dbTable = metadata.find { it.tableName == tableName } ?: return@flatMap emptyList()

        // 获取代码中定义的索引
        val codeIndexes = cls.getIndexDefinitions()

        // 获取数据库中的索引（从元数据读取）
        val dbIndexes = getDbIndexes(tableName)

        // 找出代码中有但数据库中没有的索引
        codeIndexes
            .filter { codeIndex ->
                dbIndexes.none { dbIndex ->
                    // 比较索引名或列组合是否相同
                    dbIndex.name == codeIndex.name ||
                    (dbIndex.columns.sorted() == codeIndex.columns.sorted() &&
                     dbIndex.type == codeIndex.type)
                }
            }
            .map { indexDef ->
                strategy.generateCreateIndex(tableName, indexDef)
            }
    }
}

/**
 * 需要添加的外键
 * 比对代码中的外键定义与数据库中的外键，找出需要添加的外键
 */
fun List<LsiClass>.diffForeignKeys(): List<String> {
    if (!Settings.autoddlForeignKeys) return emptyList()

    val strategy = Settings.strategy

    return flatMap { cls ->
        val tableName = cls.guessTableName

        // 获取代码中定义的外键
        val codeForeignKeys = cls.getDatabaseForeignKeys()

        // 获取数据库中的外键
        val dbForeignKeys = getDbForeignKeys(tableName)

        // 找出代码中有但数据库中没有的外键
        codeForeignKeys
            .filter { codeFk ->
                dbForeignKeys.none { dbFk ->
                    // 比较外键定义是否相同
                    dbFk.fkColumnName == codeFk.columnName &&
                    dbFk.pkTableName == codeFk.referencedTableName &&
                    dbFk.pkColumnName == codeFk.referencedColumnName
                }
            }
            .map { fk ->
                strategy.generateAddForeignKey(tableName, fk)
            }
    }
}

/**
 * 需要删除的外键
 * 找出数据库中有但代码中没有的外键（如果允许删除）
 */
fun List<LsiClass>.diffDropForeignKeys(): List<String> {
    if (!Settings.autoddlForeignKeys || !Settings.autoddlAllowDeleteColumn) return emptyList()

    val strategy = Settings.strategy

    return flatMap { cls ->
        val tableName = cls.guessTableName

        // 获取代码中定义的外键
        val codeForeignKeys = cls.getDatabaseForeignKeys()

        // 获取数据库中的外键
        val dbForeignKeys = getDbForeignKeys(tableName)

        // 找出数据库中有但代码中没有的外键
        dbForeignKeys
            .filter { dbFk ->
                codeForeignKeys.none { codeFk ->
                    dbFk.fkColumnName == codeFk.columnName &&
                    dbFk.pkTableName == codeFk.referencedTableName &&
                    dbFk.pkColumnName == codeFk.referencedColumnName
                }
            }
            .mapNotNull { dbFk ->
                dbFk.fkName?.let { fkName ->
                    "ALTER TABLE \"$tableName\" DROP CONSTRAINT \"$fkName\";"
                }
            }
    }
}

/**
 * 从数据库读取指定表的索引信息
 */
private fun getDbIndexes(tableName: String): List<site.addzero.util.lsi.database.model.IndexDefinition> {
    // 使用 JDBC 元数据读取索引
    return try {
        databaseMetadataReader.getIndexMetadata(tableName = tableName)
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * 从数据库读取指定表的外键信息
 */
private fun getDbForeignKeys(tableName: String): List<site.addzero.entity.ForeignKeyMetadata> {
    // 使用已有的 getForeignKeysMetadata 方法
    return try {
        databaseMetadataReader.getForeignKeysMetadata()
            .filter { it.fkTableName == tableName }
    } catch (e: Exception) {
        emptyList()
    }
}