package site.addzero.dict.trans.inter

/**
 * LSI字典转换器接口
 * 提供实体与字典DTO之间的双向转换功能
 * 
 * @param T 原始实体类型
 * @param D 字典DTO类型
 */
interface LsiDictConvertor<T, D> {
    
    /**
     * 将原始实体转换为包含字典翻译的DTO
     * 执行 code -> name 的字典翻译
     * 
     * @param entity 原始实体对象
     * @return 包含字典翻译文本的DTO对象
     */
    fun code2name(entity: T): D
    
    /**
     * 将字典DTO转换回原始实体
     * 执行 name -> code 的反向翻译
     * 
     * @param dto 包含字典翻译文本的DTO对象
     * @return 原始实体对象
     */
    fun name2code(dto: D): T
}