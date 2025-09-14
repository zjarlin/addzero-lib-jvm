       package site.addzero.generated.enums
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
            DAILY("DAILY", "每天定时执行"),
FIXED_RATE("FIXED_RATE", "固定间隔重复执行"),
ONCE("ONCE", "执行一次");
        }