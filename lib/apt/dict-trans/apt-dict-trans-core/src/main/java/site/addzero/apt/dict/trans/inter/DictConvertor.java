package site.addzero.apt.dict.trans.inter;

import java.util.Arrays;
import java.util.List;

/**
 * 字典转换器接口
 * 提供实体与字典DTO之间的双向转换功能
 * 
 * @param <T> 原始实体类型
 * @param <D> 字典DTO类型
 */
public interface DictConvertor<T, D> {
    
    /**
     * 将原始实体转换为包含字典翻译的DTO
     * 执行 code -> name 的字典翻译
     * 
     * @param entity 原始实体对象
     * @return 包含字典翻译文本的DTO对象
     */
    default D code2name(T entity) {
        if (entity == null) return null;
        List<T> entities = Arrays.asList(entity);
        List<D> results = codes2names(entities);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * 将字典DTO转换回原始实体
     * 执行 name -> code 的反向翻译
     * 
     * @param dto 包含字典翻译文本的DTO对象
     * @return 原始实体对象
     */
    default T name2code(D dto) {
        if (dto == null) return null;
        List<D> dtos = Arrays.asList(dto);
        List<T> results = name2codes(dtos);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * 批量将原始实体转换为包含字典翻译的DTO
     * 
     * @param entities 原始实体列表
     * @return 包含字典翻译文本的DTO列表
     */
    List<D> codes2names(List<T> entities);
    
    /**
     * 批量将字典DTO转换回原始实体
     * 
     * @param dtos 包含字典翻译文本的DTO列表
     * @return 原始实体列表
     */
    List<T> name2codes(List<D> dtos);
}