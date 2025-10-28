package site.addzero.tdengineorm.wrapper;

import cn.hutool.core.util.StrUtil;
import site.addzero.tdengineorm.constant.SqlConstant;
import site.addzero.tdengineorm.enums.TdSelectFuncEnum;
import site.addzero.tdengineorm.exception.TdOrmException;
import site.addzero.tdengineorm.exception.TdOrmExceptionCode;
import site.addzero.tdengineorm.util.AssertUtil;
import site.addzero.tdengineorm.util.TdSqlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nullen
 */
@RequiredArgsConstructor
public abstract class AbstractSelectCalc {
    protected final List<SelectCalcWrapper.SelectColumn> selectColumnList = new ArrayList<>(16);

    protected String getFinalColumnAliasName() {
        return "";
    }

    public String getFinalSelectSql() {
        if (CollectionUtils.isEmpty(selectColumnList)) {
            return StrUtil.EMPTY;
        }
        AssertUtil.notBlank(getFinalColumnAliasName(), new TdOrmException(TdOrmExceptionCode.COLUMN_NO_ALIAS_NAME));
        StringBuilder finalSelectColumn = new StringBuilder(SqlConstant.LEFT_BRACKET);
        selectColumnList.forEach(selectColumn -> {
            TdSelectFuncEnum selectFuncEnum = selectColumn.getSelectFuncEnum();
            finalSelectColumn.append(selectFuncEnum == null ? selectColumn.getColumnName()
                    : TdSqlUtil.buildAggregationFunc(selectFuncEnum, selectColumn.getColumnName(), ""));

            if (null != selectColumn.getSelectJoinSymbolSuffix()) {
                finalSelectColumn.append(selectColumn.getSelectJoinSymbolSuffix().getKey());
            }
        });

        return finalSelectColumn.append(SqlConstant.RIGHT_BRACKET).append(getFinalColumnAliasName()).toString();
    }
}
