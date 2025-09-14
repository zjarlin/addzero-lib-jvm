package site.addzero.aop.dicttrans.inter

import site.addzero.aop.dicttrans.dictaop.entity.DictModel

interface TransApi {


    /**
     * 内置字典的多翻译(code2name)
     *
     * @param dictCodes 例如：user_status,sex
     * @param keys      例如：1,2,0
     * @return
     */
    fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel>


    /**
     * 任意表的批量翻译
     * @param [table]
     * @param [text]
     * @param [code]
     * @param [keys] code列集合或者name列数据集合
     * @return [List<JSONObject>]
     */
    fun translateTableBatchCode2name(table: String, text: String, code: String, keys: String): List<Map<String, Any?>>


}
