package site.addzero.dict.trans.inter

/**
 * 翻译任务信息
 * 编译时生成表达式，运行时通过单例工厂处理
 */
data class TransTask(
    /**
     * 任务唯一ID
     */
    val taskId: String,
    
    /**
     * 字段路径（支持嵌套，如 "deviceInfo.location.testvar1"）
     */
    val fieldPath: String,
    
    /**
     * 获取原始值的表达式（编译时生成）
     * 例如: "dto.getDeviceInfo().getLocation().getTestvar1()"
     */
    val valueExpression: String,
    
    /**
     * 翻译类型：system（系统字典）或 table（表字典）
     */
    val dictType: String,
    
    /**
     * 字典配置
     * - 系统字典：dicCode
     * - 表字典：table|codeColumn|nameColumn|whereCondition
     */
    val dictConfig: String,
    
    /**
     * 设置翻译结果的表达式（编译时生成）
     * 例如: "dto.getDeviceInfo().getLocation().setTestvar1Name(translatedValue)"
     */
    val setterExpression: String,
    
    /**
     * 嵌套类前缀（如 "ComplexNestedEntity.DeviceInfo.Location"）
     */
    val nestedClassPrefix: String = "",
    
    /**
     * 任务优先级
     */
    val priority: Int = 0
) {
    /**
     * 获取任务的缓存键，用于去重和分组
     */
    fun getCacheKey(): String = "$dictType:$dictConfig"
    
    /**
     * 是否为系统字典任务
     */
    fun isSystemDict(): Boolean = dictType == "system"
    
    /**
     * 是否为表字典任务
     */
    fun isTableDict(): Boolean = dictType == "table"
}