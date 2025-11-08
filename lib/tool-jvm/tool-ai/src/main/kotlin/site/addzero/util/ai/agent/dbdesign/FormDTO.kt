package site.addzero.util.ai.agent.dbdesign

/**
 * 表单数据传输对象
 *
 * @property tableName 表中文名
 * @property tableEnglishName 表英文名称tableEnglistName业务系统要求biz_开头,如果是系统架构表需要sys_开头
 * @property dbType dbType的候选项为(全部小写字母) mysql oracle pg dm
 * @property dbName 数据库名称,达梦dm数据库类型的dbname不可以为空,其余都可以为空
 * @property fields 字段列表
 */
data class FormDTO(
    val tableName: String? = "",
    // tableEnglishName属性，用于表示表的英文名称
    val tableEnglishName: String = "",
    // dbType属性，用于表示数据库的类型
    val dbType: String = "",
    // dbName属性，用于表示数据库的名称
    val dbName: String? = "",
    // fields属性，用于表示表中的字段列表
    var fields: List<FieldDTO>? = null,
)

/**
 * 字段数据传输对象，用于在数据传输过程中承载表单字段相关的信息
 *
 * @property javaType javaType下拉框选项为 Integer |long |String| boolean| Date LocalTime |LocalDateTime |BigDecimal| double
 * @property fieldName 字段名称
 * @property fieldChineseName 字段中文名
 */
// FieldDTO类，用于在数据传输过程中承载表单字段相关的信息
data class FieldDTO(
    // javaType属性，用于表示字段对应的Java类型
    var javaType: String = "",
    // fieldName属性，用于表示字段的名称
    var fieldName: String? = null,
    // fieldChineseName属性，用于表示字段的中文名
    var fieldChineseName: String = "",
)

/**
 * 字段数据传输对象(文档用途)
 *
 * @property fieldName 字段名称
 * @property fieldChineseName 字段中文名
 */
data class FieldDTOUseDoc(
    // fieldName属性，用于表示字段的名称
    var fieldName: String? = null,
    // fieldChineseName属性，用于表示字段的中文名
    var fieldChineseName: String = "",
)

/**
 * 字段数据传输对象列表(文档用途)
 *
 * @property fieldInfo 字段信息集合
 */
data class FieldDTOUseDocList(
    var fieldInfo: List<FieldDTOUseDoc> ? = null,
)
