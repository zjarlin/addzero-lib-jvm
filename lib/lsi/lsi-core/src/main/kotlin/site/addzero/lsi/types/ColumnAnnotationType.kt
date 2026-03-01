package site.addzero.lsi.types

/**
 * 列名注解的强类型枚举
 *
 * 用于处理数据库列名相关的注解 (Column, TableField等)
 */
enum class ColumnAnnotationType(
    val fqName: String,
    val shortName: String,
    val nameAttribute: String
) {
    JIMMER_COLUMN("org.babyfish.jimmer.sql.Column", "Column", "name"),
    MYBATIS_PLUS_TABLE_FIELD("com.baomidou.mybatisplus.annotation.TableField", "TableField", "value"),
    JPA_COLUMN("javax.persistence.Column", "Column", "name"),
    JAKARTA_COLUMN("jakarta.persistence.Column", "Column", "name");

    companion object {
        private val byFqName = entries.associateBy { it.fqName }
        private val byShortName = entries.groupBy { it.shortName }

        fun findByFqName(fqName: String): ColumnAnnotationType? = byFqName[fqName]
        fun findByShortName(shortName: String): List<ColumnAnnotationType> = byShortName[shortName].orEmpty()

        val allFqNames: Set<String> get() = byFqName.keys

        /**
         * 获取注解与其列名属性名的映射
         */
        val fqNameToAttributeMap: Map<String, String> get() =
            entries.associate { it.fqName to it.nameAttribute }
    }
}
