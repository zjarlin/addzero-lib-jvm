package site.addzero.tdengineorm.wrapper;

import site.addzero.tdengineorm.enums.SelectJoinSymbolEnum;
import lombok.RequiredArgsConstructor;

/**
 * @author Nullen
 */
@RequiredArgsConstructor
public class SelectCalcSymbol<T> extends AbstractSelectCalc {

    private final SelectCalcWrapper<T> selectCalcWrapper;

    public SelectCalcWrapper<T> operate(SelectJoinSymbolEnum selectJoinSymbolEnum) {
        selectCalcWrapper.operate(selectJoinSymbolEnum);
        return this.selectCalcWrapper;
    }

    public SelectCalcWrapper<T> setFinalColumnAliasName(String aliasName) {
        selectCalcWrapper.setFinalColumnAliasName(aliasName);
        return selectCalcWrapper;
    }

}
