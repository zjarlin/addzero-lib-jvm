        package com.addzero.generated.enums
        
        /**
         * 抽屉方向枚举
         * 
         * 数据库字典编码: DRAWER_DIRECTION
         * 自动生成的枚举类，不要手动修改
         */
        enum class EnumDRAWERDIRECTION(
            val code: String,
            val desc: String
        ) {
            CONG_DI_BU_HUA_CHU("BOTTOM", "从底部滑出"),
CONG_ZUO_CE_HUA_CHU("LEFT", "从左侧滑出"),
CONG_YOU_CE_HUA_CHU("RIGHT", "从右侧滑出"),
CONG_DING_BU_HUA_CHU("TOP", "从顶部滑出");
            
            companion object {
                /**
                 * 根据编码获取枚举值
                 * 
                 * @param code 编码
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromCode(code: String): EnumDRAWERDIRECTION? {
                    return entries.find { it.code == code }
                }
                
                /**
                 * 根据描述获取枚举值
                 * 
                 * @param desc 描述
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromDesc(desc: String): EnumDRAWERDIRECTION? {
                    return entries.find { it.desc == desc }
                }
            }
        }