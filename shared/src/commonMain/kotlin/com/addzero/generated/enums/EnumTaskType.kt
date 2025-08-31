        package com.addzero.generated.enums
        
        /**
         * 任务类型枚举
         * 
         * 数据库字典编码: TaskType
         * 自动生成的枚举类，不要手动修改
         */
        enum class EnumTaskType(
            val code: String,
            val desc: String
        ) {
            MEI_TIAN_DING_SHI_ZHIXING("DAILY", "每天定时执行"),
GU_DING_JIAN_GE_CHONG_FU_ZHIXING("FIXED_RATE", "固定间隔重复执行"),
ZHI_XING_YICI("ONCE", "执行一次");
            
            companion object {
                /**
                 * 根据编码获取枚举值
                 * 
                 * @param code 编码
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromCode(code: String): EnumTaskType? {
                    return entries.find { it.code == code }
                }
                
                /**
                 * 根据描述获取枚举值
                 * 
                 * @param desc 描述
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromDesc(desc: String): EnumTaskType? {
                    return entries.find { it.desc == desc }
                }
            }
        }