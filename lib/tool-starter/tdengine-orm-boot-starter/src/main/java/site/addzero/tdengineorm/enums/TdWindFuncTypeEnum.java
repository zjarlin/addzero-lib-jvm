package site.addzero.tdengineorm.enums;

import site.addzero.tdengineorm.constant.TdSqlConstant;
import lombok.Getter;

/**
 * @author Nullen
 */
@Getter
public enum TdWindFuncTypeEnum {

    /**
     * 时间窗口函数
     */
    INTERVAL(TdSqlConstant.INTERVAL),

    /**
     * 状态窗口函数
     */
    STATE_WINDOW(TdSqlConstant.STATE_WINDOW);

    private final String key;

    TdWindFuncTypeEnum(String key) {
        this.key = key;
    }
}
