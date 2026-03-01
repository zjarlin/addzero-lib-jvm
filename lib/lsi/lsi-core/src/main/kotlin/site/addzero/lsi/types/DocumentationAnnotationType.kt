package site.addzero.lsi.types

/**
 * 文档注解的强类型枚举
 *
 * 用于处理Swagger、Excel等文档类注解，提供统一的描述信息提取
 */
enum class DocumentationAnnotationType(
    val fqName: String,
    val shortName: String,
    val descriptionAttribute: String
) {
    SWAGGER_V2_API_MODEL_PROPERTY("io.swagger.annotations.ApiModelProperty", "ApiModelProperty", "value"),
    SWAGGER_V3_SCHEMA("io.swagger.v3.oas.annotations.media.Schema", "Schema", "description"),
    EXCEL_PROPERTY_ALIBABA("com.alibaba.excel.annotation.ExcelProperty", "ExcelProperty", "value"),
    EXCEL_PROPERTY_IDEV("cn.idev.excel.annotation.ExcelProperty", "ExcelProperty", "value"),
    EXCEL_EASYPOI("cn.afterturn.easypoi.excel.annotation.Excel", "Excel", "name");

    companion object {
        private val byFqName = entries.associateBy { it.fqName }
        private val byShortName = entries.groupBy { it.shortName }

        fun findByFqName(fqName: String): DocumentationAnnotationType? = byFqName[fqName]
        fun findByShortName(shortName: String): List<DocumentationAnnotationType> = byShortName[shortName].orEmpty()

        val allFqNames: Set<String> get() = byFqName.keys

        /**
         * 获取注解与其描述属性名的映射
         */
        val fqNameToAttributeMap: Map<String, String> get() =
            entries.associate { it.fqName to it.descriptionAttribute }
    }
}
