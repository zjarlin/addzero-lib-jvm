package site.addzero.apt.dict.trans.registry;

import site.addzero.apt.dict.trans.inter.DictConvertor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DictConvertor registry for managing entity/DTO class to convertor mappings
 * Thread-safe singleton registry
 */
public class DictConvertorRegistry {

    private static final Map<Class<?>, DictConvertor<?, ?>> entityToConvertorMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, DictConvertor<?, ?>> dtoToConvertorMap = new ConcurrentHashMap<>();

    private DictConvertorRegistry() {
    }

    /**
     * Register a convertor for given entity and DTO classes
     *
     * @param entityClass entity class type
     * @param dtoClass    DTO class type
     * @param convertor   convertor instance
     */
    public static void register(Class<?> entityClass, Class<?> dtoClass, DictConvertor<?, ?> convertor) {
        if (entityClass == null || dtoClass == null || convertor == null) {
            throw new IllegalArgumentException("entityClass, dtoClass and convertor cannot be null");
        }
        entityToConvertorMap.put(entityClass, convertor);
        dtoToConvertorMap.put(dtoClass, convertor);
    }

    /**
     * Get convertor by entity class
     *
     * @param entityClass entity class type
     * @return convertor instance or null if not found
     */
    public static DictConvertor<?, ?> getConvertorByEntityClass(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        return entityToConvertorMap.get(entityClass);
    }

    /**
     * Get convertor by DTO class
     *
     * @param dtoClass DTO class type
     * @return convertor instance or null if not found
     */
    public static DictConvertor<?, ?> getConvertorByDtoClass(Class<?> dtoClass) {
        if (dtoClass == null) {
            return null;
        }
        return dtoToConvertorMap.get(dtoClass);
    }

    /**
     * Clear all registered convertors (for testing purposes)
     */
    public static void clear() {
        entityToConvertorMap.clear();
        dtoToConvertorMap.clear();
    }

    /**
     * Get number of registered convertors
     *
     * @return number of registered convertors
     */
    public static int size() {
        return entityToConvertorMap.size();
    }
}
