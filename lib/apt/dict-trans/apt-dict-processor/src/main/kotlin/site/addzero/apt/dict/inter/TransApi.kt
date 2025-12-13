package site.addzero.apt.dict.inter

import site.addzero.apt.dict.model.DictModel

/**
 * 字典翻译API接口
 * 编译时生成的代码将调用此接口进行字典翻译
 * 用户只需要提供批量查询接口，消除N+1查询问题
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

