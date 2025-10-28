package site.addzero.tdengineorm.util;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 */
public class StringUtil {

    /**
     * 为字符串添加前后缀
     *
     * @param str 字符串
     * @param fix 前后缀
     * @return 添加前后缀后的字符串
     */
    public static String makeSurroundWith(String str, String fix) {
        if (str == null) {
            return null;
        }

        String result = StrUtil.addPrefixIfNot(str, fix);
        result = StrUtil.addSuffixIfNot(result, fix);
        return result;
    }

    /**
     * 为字符串添加前后缀
     *
     * @param str 字符串
     * @param fix 前后缀
     * @return 添加前后缀后的字符串
     */
    public static String makeSurroundWithNullable(String str, String fix) {
        if (str == null) {
            return null;
        }

        String result = StrUtil.addPrefixIfNot(str, fix);
        result = StrUtil.addSuffixIfNot(result, fix);
        return result;
    }
}
