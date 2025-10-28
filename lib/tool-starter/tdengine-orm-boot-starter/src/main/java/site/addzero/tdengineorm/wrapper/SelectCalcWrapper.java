package site.addzero.tdengineorm.wrapper;

import site.addzero.tdengineorm.enums.SelectJoinSymbolEnum;
import site.addzero.tdengineorm.enums.TdSelectFuncEnum;
import site.addzero.tdengineorm.exception.TdOrmException;
import site.addzero.tdengineorm.exception.TdOrmExceptionCode;
import site.addzero.tdengineorm.func.GetterFunction;
import site.addzero.tdengineorm.util.AssertUtil;
import site.addzero.tdengineorm.util.TdSqlUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


/**
 * @author Nullen
 */
@RequiredArgsConstructor
public class SelectCalcWrapper<T> extends AbstractSelectCalc {
    private final Class<T> entityClass;
    private final SelectCalcSymbol<T> selectCalcSymbol = new SelectCalcSymbol<>(this);
    private String finalColumnAliasName;

    public SelectCalcSymbol<T> select(String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).build());
        return this.selectCalcSymbol;
    }

    public final SelectCalcSymbol<T> select(GetterFunction<T, ?> getterFunc) {
        return this.select(TdSqlUtil.getColumnName(entityClass, getterFunc));
    }

    public SelectCalcSymbol<T> select(TdSelectFuncEnum selectFuncEnum, String columnName) {
        addSelect(SelectColumn.builder().columnName(columnName).selectFuncEnum(selectFuncEnum).build());
        return this.selectCalcSymbol;
    }

    public SelectCalcSymbol<T> select(TdSelectFuncEnum selectFuncEnum, GetterFunction<T, ?> getterFunc) {
        return select(selectFuncEnum, TdSqlUtil.getColumnName(this.entityClass, getterFunc));
    }

    /**
     * 给最后一个字段增加操作符
     *
     * @param selectJoinSymbolEnum 选择连接符号enum
     */
    void operate(SelectJoinSymbolEnum selectJoinSymbolEnum) {
        AssertUtil.notEmpty(selectColumnList, new TdOrmException(TdOrmExceptionCode.NO_SELECT));
        SelectColumn lastOne = selectColumnList.get(selectColumnList.size() - 1);
        lastOne.selectJoinSymbolSuffix = selectJoinSymbolEnum;
    }

    private void addSelect(SelectColumn selectColumn) {
        selectColumnList.add(selectColumn);
    }

    public void setFinalColumnAliasName(GetterFunction<T, ?> aliasColumn) {
        setFinalColumnAliasName(TdSqlUtil.getColumnName(entityClass, aliasColumn));
    }

    public void setFinalColumnAliasName(String aliasName) {
        finalColumnAliasName = aliasName;
    }

    @Override
    protected String getFinalColumnAliasName() {
        return finalColumnAliasName;
    }

    @Data
    @Builder
    static class SelectColumn {
        private TdSelectFuncEnum selectFuncEnum;
        private String columnName;
        private SelectJoinSymbolEnum selectJoinSymbolSuffix;
    }
}
