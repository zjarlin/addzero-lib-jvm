package site.addzero.tdengineorm.strategy;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import site.addzero.tdengineorm.annotation.TdTable;
import site.addzero.tdengineorm.util.TdSqlUtil;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 直接返回原表名称
 *
 * @author Silas
 */
public class DefaultDynamicNameStrategy implements DynamicNameStrategy<Object> {

    @Override
    public String dynamicTableName(Object entity) {
        String superTableName = TdSqlUtil.getTbName(entity.getClass());
        //暂时子表前缀命名
        String tablePrefix=superTableName;
        Set<Pair<String, String>> allTagFieldsPair = TdSqlUtil.getAllTagFieldsPair(entity);
        String collect = allTagFieldsPair.stream().map(e -> {
            String fieldName = e.getKey();
            String fieldValue = e.getValue();
            return fieldValue;
        }).collect(Collectors.joining("_"));
        String join = StrUtil.join("_", tablePrefix, collect);
        return join;
    }
}
