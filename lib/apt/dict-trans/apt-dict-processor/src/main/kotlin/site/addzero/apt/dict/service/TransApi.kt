package site.addzero.apt.dict.service

import site.addzero.apt.dict.model.DictModel

/**
 * Dictionary translation API interface for compile-time generated translation code
 * 
 * This interface defines the contract for dictionary translation services
 * that work with the compile-time generated enhanced entities.
 * Users only need to provide batch query interfaces to eliminate N+1 query problems.
 */
interface TransApi {
    
    /**
     * 内置字典的批量翻译(code2name)
     * 
     * @param dictCodes 例如：user_status,sex
     * @param keys      例如：1,2,0
     * @return List of DictModel containing translation results
     */
    fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel>
    
    /**
     * 任意表的批量翻译
     * 
     * @param table 表名
     * @param text  名称列
     * @param code  编码列
     * @param keys  code列集合或者name列数据集合
     * @return List of Map containing translation results
     */
    fun translateTableBatchCode2name(table: String, text: String, code: String, keys: String): List<Map<String, Any?>>
}

