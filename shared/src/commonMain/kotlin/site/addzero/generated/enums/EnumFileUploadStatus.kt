       package site.addzero.generated.enums
        /**
         * 文件上传状态
         * 
         * 数据库字典编码: file_upload_status
         * 自动生成的枚举类，不要手动修改
         */
        enum class EnumFileUploadStatus(
            val code: String,
            val desc: String
        ) {
            FAILED("FAILED", "上传失败"),
IDLE("IDLE", "空闲状态"),
SUCCESS("SUCCESS", "上传成功"),
UPLOADING("UPLOADING", "上传中");
        }