        package com.addzero.generated.enums
        
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
            SHANG_CHUAN_SHIBAI("FAILED", "上传失败"),
KONG_XIAN_ZHUANG_TAI("IDLE", "空闲状态"),
SHANG_CHUAN_CHENGGONG("SUCCESS", "上传成功"),
SHANG_CHUANZHONG("UPLOADING", "上传中");
            
            companion object {
                /**
                 * 根据编码获取枚举值
                 * 
                 * @param code 编码
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromCode(code: String): EnumFileUploadStatus? {
                    return entries.find { it.code == code }
                }
                
                /**
                 * 根据描述获取枚举值
                 * 
                 * @param desc 描述
                 * @return 对应的枚举值，如果不存在则返回null
                 */
                fun fromDesc(desc: String): EnumFileUploadStatus? {
                    return entries.find { it.desc == desc }
                }
            }
        }