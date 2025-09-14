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
            BOTTOM("BOTTOM", "从底部滑出"),
LEFT("LEFT", "从左侧滑出"),
RIGHT("RIGHT", "从右侧滑出"),
TOP("TOP", "从顶部滑出");
            
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