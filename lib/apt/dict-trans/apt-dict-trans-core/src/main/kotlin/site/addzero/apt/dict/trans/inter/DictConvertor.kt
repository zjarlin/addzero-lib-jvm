package site.addzero.apt.dict.trans.inter

/**
 * 字典转换器接口
 * 提供实体与字典DTO之间的双向转换功能
 * 
 * @param T 原始实体类型
 * @param D 字典DTO类型
 */
interface DictConvertor<T, D> {
    
    /**
     * 将原始实体转换为包含字典翻译的DTO
     * 执行 code -> name 的字典翻译
     * 
     * @param entity 原始实体对象
     * @return 包含字典翻译文本的DTO对象
     */
    fun code2name(entity: T?): D?{
        entity?:return null
        val listOf = listOf(entity)
        val codes2names = codes2names(listOf)
        val firstOrNull = codes2names.firstOrNull()
        return firstOrNull
    }
    
    /**
     * 将字典DTO转换回原始实体
     * 执行 name -> code 的反向翻译
     * 
     * @param dto 包含字典翻译文本的DTO对象
     * @return 原始实体对象
     */
    fun name2code(dto: D?): T?{
        dto?:return null
        val listOf = listOf(dto)
        val firstOrNull = name2codes(listOf).firstOrNull()
        return firstOrNull
    }




    fun codes2names(entitys: List<T?>): List<D?>


    fun name2codes(dtos:List<D?> ): List<T?>
}