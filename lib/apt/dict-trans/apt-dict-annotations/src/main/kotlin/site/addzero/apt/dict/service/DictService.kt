package site.addzero.apt.dict.service

/**
 * 字典翻译服务接口
 * 编译时生成的代码将调用此接口进行字典翻译
 */
interface DictService {
    
    /**
     * 根据字典编码和键值翻译单个值
     */
    fun translateByDictCode(dictCode: String, key: String?): String?
    
    /**
     * 根据字典编码批量翻译
     */
    fun translateBatchByDictCode(dictCode: String, keys: List<String?>): Map<String?, String?>
    
    /**
     * 根据自定义表翻译单个值
     */
    fun translateByTable(table: String, codeColumn: String, nameColumn: String, key: Any?): String?
    
    /**
     * 根据自定义表批量翻译
     */
    fun translateBatchByTable(
        table: String, 
        codeColumn: String, 
        nameColumn: String, 
        keys: List<Any?>
    ): Map<Any?, String?>
}

/**
 * 字典翻译结果
 */
data class DictTranslateResult(
    val success: Boolean,
    val value: String?,
    val error: String? = null
)

/**
 * 批量翻译请求
 */
data class BatchTranslateRequest(
    val dictCode: String? = null,
    val table: String? = null,
    val codeColumn: String? = null,
    val nameColumn: String? = null,
    val keys: List<Any?>
)