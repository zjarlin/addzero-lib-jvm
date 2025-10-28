package site.addzero.tdengineorm.strategy;

/**
 * 动态表名称策略
 *
 * @author Silas
 */
@FunctionalInterface
public interface DynamicNameStrategy<T> {

    /**
     * 动态表名生成
     *
     * @return 根据策略修改后的表名
     */
    String dynamicTableName(T entity);

}
