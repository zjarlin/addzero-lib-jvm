package site.addzero.apt.dict.trans.util;

import site.addzero.apt.dict.trans.inter.DictConvertor;
import site.addzero.apt.dict.trans.registry.DictConvertorRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dictionary translation utility class
 * Provides static methods for easy entity/DTO conversion
 */
public class DictUtil {

    private DictUtil() {
    }

    /**
     * Convert entities to DTOs with dictionary translation (code to name)
     *
     * @param entities list of entities
     * @param <T>      entity type
     * @param <D>      DTO type
     * @return list of DTOs with dictionary translations
     */
    @SuppressWarnings("unchecked")
    public static <T, D> List<D> code2names(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        Class<?> entityClass = entities.get(0).getClass();
        DictConvertor<T, D> convertor = (DictConvertor<T, D>)
                DictConvertorRegistry.getConvertorByEntityClass(entityClass);

        if (convertor == null) {
            throw new IllegalStateException(
                    "No DictConvertor found for entity class: " + entityClass.getName() +
                            ". Make sure the convertor is registered as a Spring bean."
            );
        }

        return convertor.codes2names(entities);
    }

    /**
     * Convert single entity to DTO with dictionary translation (code to name)
     *
     * @param entity single entity
     * @param <T>    entity type
     * @param <D>    DTO type
     * @return DTO with dictionary translations
     */
    public static <T, D> D code2name(T entity) {
        if (entity == null) {
            return null;
        }
        List<D> results = code2names(Arrays.asList(entity));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Convert DTOs to entities with reverse dictionary translation (name to code)
     *
     * @param dtos list of DTOs
     * @param <T>  entity type
     * @param <D>  DTO type
     * @return list of entities
     */
    @SuppressWarnings("unchecked")
    public static <T, D> List<T> name2codes(List<D> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        Class<?> dtoClass = dtos.get(0).getClass();
        DictConvertor<T, D> convertor = (DictConvertor<T, D>)
                DictConvertorRegistry.getConvertorByDtoClass(dtoClass);

        if (convertor == null) {
            throw new IllegalStateException(
                    "No DictConvertor found for DTO class: " + dtoClass.getName() +
                            ". Make sure the convertor is registered as a Spring bean."
            );
        }

        return convertor.name2codes(dtos);
    }

    /**
     * Convert single DTO to entity with reverse dictionary translation (name to code)
     *
     * @param dto single DTO
     * @param <T> entity type
     * @param <D> DTO type
     * @return entity
     */
    public static <T, D> T name2code(D dto) {
        if (dto == null) {
            return null;
        }
        List<T> results = name2codes(Arrays.asList(dto));
        return results.isEmpty() ? null : results.get(0);
    }
}
