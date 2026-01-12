package site.addzero.util.lsi.database.dialect

import site.addzero.util.db.DatabaseType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.lsi.database.model.ForeignKeyInfo
import site.addzero.util.lsi.database.model.IndexDefinition
import site.addzero.util.lsi.database.model.IndexType
import site.addzero.util.lsi.database.model.ManyToManyTable
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi_impl.impl.database.clazz.getAllDbFields
import site.addzero.util.lsi_impl.impl.database.clazz.getDatabaseForeignKeys

interface DdlGenerationStrategy {

    /**
     * 检查此策略是否支持给定的数据库方言
     */
    fun support(databaseType: DatabaseType): Boolean

    /**
     * 根据 JDBC 类型代码获取数据库原生类型名称
     *
     * @param jdbcType JDBC 类型代码 (java.sql.Types.*)
     * @return 数据库原生类型名称，例如 "INT", "VARCHAR(255)", "TEXT" 等
     */
    fun getColumnTypeString(lsiField: LsiField): String = "VARCHAR(255)"

    /**
     * 标识符引号
     * @return 引号字符，例如 MySQL 的 "`"，PostgreSQL 的 "
     */
    val quoteIdentifier: String
        get() = "`"

    /**
     * 生成创建表的DDL语句
     */
    fun generateCreateTable(klass: LsiClass): String

    /**
     * 生成删除表的DDL语句
     */
    fun generateDropTable(tableName: String): String

    /**
     * 生成添加列的DDL语句
     */
    fun generateAddColumn(tableName: String, field: LsiField): String

    /**
     * 生成删除列的DDL语句
     */
    fun generateDropColumn(tableName: String, columnName: String): String

    /**
     * 生成修改列的DDL语句
     */
    fun generateModifyColumn(tableName: String, field: LsiField): String

    /**
     * 生成添加外键约束的DDL语句
     */
    fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String

    /**
     * 生成添加注释的DDL语句
     */
    fun generateAddComment(lsiClass: LsiClass): String

    /**
     * 生成创建索引的DDL语句
     */
    fun generateCreateIndex(tableName: String,index:IndexDefinition): String {
        val indexType = if (index.type == IndexType.UNIQUE) "UNIQUE INDEX" else "INDEX"
        val columns = index.columns.joinToString(", ") { it.makeSurroundWith(quoteIdentifier) }
        return "CREATE $indexType ${index.name.makeSurroundWith(quoteIdentifier)} ON ${tableName.makeSurroundWith(quoteIdentifier)} ($columns);"
    }

    /**
     * 生成多对多中间表的DDL语句（不包含外键）
     * 外键应该在所有表创建完成后单独添加
     */
    fun generateManyToManyTable(table: ManyToManyTable): String {
        val tableName = table.tableName.makeSurroundWith(quoteIdentifier)
        val leftColumn = table.leftColumnName.makeSurroundWith(quoteIdentifier)
        val rightColumn = table.rightColumnName.makeSurroundWith(quoteIdentifier)
        return """
            |CREATE TABLE $tableName (
            |  $leftColumn BIGINT NOT NULL,
            |  $rightColumn BIGINT NOT NULL,
            |  PRIMARY KEY ($leftColumn, $rightColumn)
            |);
        """.trimMargin()
    }

    /**
     * 为多对多中间表生成外键约束
     */
    fun generateManyToManyTableForeignKeys(table: ManyToManyTable): List<String> {
        val tableName = table.tableName.makeSurroundWith(quoteIdentifier)
        val leftColumn = table.leftColumnName.makeSurroundWith(quoteIdentifier)
        val rightColumn = table.rightColumnName.makeSurroundWith(quoteIdentifier)
        val leftConstraint = "fk_${table.tableName}_${table.leftColumnName}".makeSurroundWith(quoteIdentifier)
        val rightConstraint = "fk_${table.tableName}_${table.rightColumnName}".makeSurroundWith(quoteIdentifier)
        val leftReference = table.leftTableName.makeSurroundWith(quoteIdentifier)
        val rightReference = table.rightTableName.makeSurroundWith(quoteIdentifier)
        val idColumn = "id".makeSurroundWith(quoteIdentifier)
        return listOf(
            "ALTER TABLE $tableName ADD CONSTRAINT $leftConstraint FOREIGN KEY ($leftColumn) REFERENCES $leftReference ($idColumn);",
            "ALTER TABLE $tableName ADD CONSTRAINT $rightConstraint FOREIGN KEY ($rightColumn) REFERENCES $rightReference ($idColumn);"
        )
    }

    /**
     * 生成基于多个 LSI 类的完整DDL语句（考虑表之间的依赖关系）
     */
    fun generateAll(lsiClasses: List<LsiClass>): String {
        // 默认实现：先创建所有表，然后添加外键约束和注释
        val createTableStatements = lsiClasses.map { lsiClass -> generateCreateTable(lsiClass) }
        val addConstraintsStatements = lsiClasses.flatMap { lsiClass ->
            val foreignKeyStatements = lsiClass.getDatabaseForeignKeys().map { fk ->
                generateAddForeignKey(lsiClass.name ?: "", fk)
            }
            val commentStatements =
                if (lsiClass.comment != null || lsiClass.getAllDbFields().any { it.comment != null }) {
                    listOf(generateAddComment(lsiClass))
                } else {
                    emptyList()
                }
            foreignKeyStatements + commentStatements
        }

        return (createTableStatements + addConstraintsStatements).joinToString("\n\n")
    }

}
