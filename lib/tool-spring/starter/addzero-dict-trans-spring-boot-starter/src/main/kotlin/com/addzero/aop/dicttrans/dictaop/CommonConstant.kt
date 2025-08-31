package com.addzero.aop.dicttrans.dictaop

interface CommonConstant {
    companion object {
        /**
         * 正常状态
         */
        const val STATUS_NORMAL: Int = 0

        /**
         * 禁用状态
         */
        val STATUS_DISABLE: Int = -1

        /**
         * 删除标志
         */
        const val DEL_FLAG_1: Int = 1

        /**
         * 未删除
         */
        const val DEL_FLAG_0: Int = 0

        /**
         * 系统日志类型： 登录
         */
        const val LOG_TYPE_1: Int = 1

        /**
         * 系统日志类型： 操作
         */
        const val LOG_TYPE_2: Int = 2

        /**
         * 操作日志类型： 查询
         */
        const val OPERATE_TYPE_1: Int = 1

        /**
         * 操作日志类型： 添加
         */
        const val OPERATE_TYPE_2: Int = 2

        /**
         * 操作日志类型： 更新
         */
        const val OPERATE_TYPE_3: Int = 3

        /**
         * 操作日志类型： 删除
         */
        const val OPERATE_TYPE_4: Int = 4

        /**
         * 操作日志类型： 倒入
         */
        const val OPERATE_TYPE_5: Int = 5

        /**
         * 操作日志类型： 导出
         */
        const val OPERATE_TYPE_6: Int = 6


        /** `500 Server Error` (HTTP/1.0 - RFC 1945)  */
        const val SC_INTERNAL_SERVER_ERROR_500: Int = 500

        /** `200 OK` (HTTP/1.0 - RFC 1945)  */
        const val SC_OK_200: Int = 200

        /**访问权限认证未通过 510 */
        const val SC_JEECG_NO_AUTHZ: Int = 510

        /** 登录用户Shiro权限缓存KEY前缀  */
        const val PREFIX_USER_SHIRO_CACHE: String =
            "shiro:cache:com.addzero.config.shiro.ShiroRealm.authorizationCache:"

        /** 登录用户Token令牌缓存KEY前缀  */
        const val PREFIX_USER_TOKEN: String = "prefix_user_token_"

        /** Token缓存时间：3600秒即一小时  */
        const val TOKEN_EXPIRE_TIME: Int = 3600

        /** 登录二维码  */
        const val LOGIN_QRCODE_PRE: String = "QRCODELOGIN:"
        const val LOGIN_QRCODE: String = "LQ:"

        /** 登录二维码token  */
        const val LOGIN_QRCODE_TOKEN: String = "LQT:"


        /**
         * 0：一级菜单
         */
        const val MENU_TYPE_0: Int = 0

        /**
         * 1：子菜单
         */
        const val MENU_TYPE_1: Int = 1

        /**
         * 2：按钮权限
         */
        const val MENU_TYPE_2: Int = 2

        /**通告对象类型（USER:指定用户，ALL:全体用户） */
        const val MSG_TYPE_UESR: String = "USER"
        const val MSG_TYPE_ALL: String = "ALL"

        /**发布状态（0未发布，1已发布，2已撤销） */
        const val NO_SEND: String = "0"
        const val HAS_SEND: String = "1"
        const val HAS_CANCLE: String = "2"

        /**阅读状态（0未读，1已读） */
        const val HAS_READ_FLAG: String = "1"
        const val NO_READ_FLAG: String = "0"

        /**优先级（L低，M中，H高） */
        const val PRIORITY_L: String = "L"
        const val PRIORITY_M: String = "M"
        const val PRIORITY_H: String = "H"

        /**
         * 短信模板方式  0 .登录模板、1.注册模板、2.忘记密码模板
         */
        const val SMS_TPL_TYPE_0: String = "0"
        const val SMS_TPL_TYPE_1: String = "1"
        const val SMS_TPL_TYPE_2: String = "2"

        /**
         * 状态(0无效1有效)
         */
        const val STATUS_0: String = "0"
        const val STATUS_1: String = "1"

        /**
         * 同步工作流引擎1同步0不同步
         */
        const val ACT_SYNC_1: Int = 1
        const val ACT_SYNC_0: Int = 0

        /**
         * 消息类型1:通知公告2:系统消息
         */
        const val MSG_CATEGORY_1: String = "1"
        const val MSG_CATEGORY_2: String = "2"

        /**
         * 是否配置菜单的数据权限 1是0否
         */
        const val RULE_FLAG_0: Int = 0
        const val RULE_FLAG_1: Int = 1

        /**
         * 是否用户已被冻结 1正常(解冻) 2冻结
         */
        const val USER_UNFREEZE: Int = 1
        const val USER_FREEZE: Int = 2

        /**字典翻译文本后缀 */
        const val DICT_TEXT_SUFFIX: String = "_dictText"

        /**
         * 表单设计器主表类型
         */
        const val DESIGN_FORM_TYPE_MAIN: Int = 1

        /**
         * 表单设计器子表表类型
         */
        const val DESIGN_FORM_TYPE_SUB: Int = 2

        /**
         * 表单设计器URL授权通过
         */
        const val DESIGN_FORM_URL_STATUS_PASSED: Int = 1

        /**
         * 表单设计器URL授权未通过
         */
        const val DESIGN_FORM_URL_STATUS_NOT_PASSED: Int = 2

        /**
         * 表单设计器新增 Flag
         */
        const val DESIGN_FORM_URL_TYPE_ADD: String = "add"

        /**
         * 表单设计器修改 Flag
         */
        const val DESIGN_FORM_URL_TYPE_EDIT: String = "edit"

        /**
         * 表单设计器详情 Flag
         */
        const val DESIGN_FORM_URL_TYPE_DETAIL: String = "detail"

        /**
         * 表单设计器复用数据 Flag
         */
        const val DESIGN_FORM_URL_TYPE_REUSE: String = "reuse"

        /**
         * 表单设计器编辑 Flag （已弃用）
         */
        const val DESIGN_FORM_URL_TYPE_VIEW: String = "view"

        /**
         * online参数值设置（是：Y, 否：N）
         */
        const val ONLINE_PARAM_VAL_IS_TURE: String = "Y"
        const val ONLINE_PARAM_VAL_IS_FALSE: String = "N"

        /**
         * 文件上传类型（本地：local，Minio：minio，阿里云：alioss）
         */
        const val UPLOAD_TYPE_LOCAL: String = "local"
        const val UPLOAD_TYPE_MINIO: String = "minio"
        const val UPLOAD_TYPE_OSS: String = "alioss"

        /**
         * 文档上传自定义桶名称
         */
        const val UPLOAD_CUSTOM_BUCKET: String = "eoafile"

        /**
         * 文档上传自定义路径
         */
        const val UPLOAD_CUSTOM_PATH: String = "eoafile"

        /**
         * 文件外链接有效天数
         */
        const val UPLOAD_EFFECTIVE_DAYS: Int = 1

        /**
         * 员工身份 （1:普通员工  2:上级）
         */
        const val USER_IDENTITY_1: Int = 1
        const val USER_IDENTITY_2: Int = 2

        /** sys_user 表 username 唯一键索引  */
        const val SQL_INDEX_UNIQ_SYS_USER_USERNAME: String = "uniq_sys_user_username"

        /** sys_user 表 work_no 唯一键索引  */
        const val SQL_INDEX_UNIQ_SYS_USER_WORK_NO: String = "uniq_sys_user_work_no"

        /** sys_user 表 phone 唯一键索引  */
        const val SQL_INDEX_UNIQ_SYS_USER_PHONE: String = "uniq_sys_user_phone"

        /** 达梦数据库升提示。违反表[SYS_USER]唯一性约束  */
        const val SQL_INDEX_UNIQ_SYS_USER: String = "唯一性约束"

        /** sys_user 表 email 唯一键索引  */
        const val SQL_INDEX_UNIQ_SYS_USER_EMAIL: String = "uniq_sys_user_email"

        /** sys_quartz_job 表 job_class_name 唯一键索引  */
        const val SQL_INDEX_UNIQ_JOB_CLASS_NAME: String = "uniq_job_class_name"

        /** sys_position 表 code 唯一键索引  */
        const val SQL_INDEX_UNIQ_CODE: String = "uniq_code"

        /** sys_role 表 code 唯一键索引  */
        const val SQL_INDEX_UNIQ_SYS_ROLE_CODE: String = "uniq_sys_role_role_code"

        /** sys_depart 表 code 唯一键索引  */
        const val SQL_INDEX_UNIQ_DEPART_ORG_CODE: String = "uniq_depart_org_code"

        /** sys_category 表 code 唯一键索引  */
        const val SQL_INDEX_UNIQ_CATEGORY_CODE: String = "idx_sc_code"

        /**
         * 在线聊天 是否为默认分组
         */
        const val IM_DEFAULT_GROUP: String = "1"

        /**
         * 在线聊天 图片文件保存路径
         */
        const val IM_UPLOAD_CUSTOM_PATH: String = "imfile"

        /**
         * 在线聊天 用户状态
         */
        const val IM_STATUS_ONLINE: String = "online"

        /**
         * 在线聊天 SOCKET消息类型
         */
        const val IM_SOCKET_TYPE: String = "chatMessage"

        /**
         * 在线聊天 是否开启默认添加好友 1是 0否
         */
        const val IM_DEFAULT_ADD_FRIEND: String = "1"

        /**
         * 在线聊天 用户好友缓存前缀
         */
        const val IM_PREFIX_USER_FRIEND_CACHE: String = "sys:cache:im:im_prefix_user_friend_"

        /**
         * 考勤补卡业务状态 （1：同意  2：不同意）
         */
        const val SIGN_PATCH_BIZ_STATUS_1: String = "1"
        const val SIGN_PATCH_BIZ_STATUS_2: String = "2"

        /**
         * 公文文档上传自定义路径
         */
        const val UPLOAD_CUSTOM_PATH_OFFICIAL: String = "officialdoc"

        /**
         * 公文文档下载自定义路径
         */
        const val DOWNLOAD_CUSTOM_PATH_OFFICIAL: String = "officaldown"

        /**
         * WPS存储值类别(1 code文号 2 text（WPS模板还是公文发文模板）)
         */
        const val WPS_TYPE_1: String = "1"
        const val WPS_TYPE_2: String = "2"


        const val X_ACCESS_TOKEN: String = "X-Access-Token"
        const val X_SIGN: String = "X-Sign"
        const val X_TIMESTAMP: String = "X-TIMESTAMP"
        const val TOKEN_IS_INVALID_MSG: String = "Token失效，请重新登录!"

        /**
         * 多租户 请求头
         */
        const val TENANT_ID: String = "tenant-id"

        /**
         * 微服务读取配置文件属性 服务地址
         */
        const val CLOUD_SERVER_KEY: String = "spring.cloud.nacos.discovery.server-addr"

        /**
         * 第三方登录 验证密码/创建用户 都需要设置一个操作码 防止被恶意调用
         */
        const val THIRD_LOGIN_CODE: String = "third_login_code"

        /**
         * 第三方APP同步方向：本地 --> 第三方APP
         */
        const val THIRD_SYNC_TO_APP: String = "SYNC_TO_APP"

        /**
         * 第三方APP同步方向：第三方APP --> 本地
         */
        const val THIRD_SYNC_TO_LOCAL: String = "SYNC_TO_LOCAL"

        /** 系统通告消息状态：0=未发布  */
        const val ANNOUNCEMENT_SEND_STATUS_0: String = "0"

        /** 系统通告消息状态：1=已发布  */
        const val ANNOUNCEMENT_SEND_STATUS_1: String = "1"

        /** 系统通告消息状态：2=已撤销  */
        const val ANNOUNCEMENT_SEND_STATUS_2: String = "2"

        /**ONLINE 报表权限用 从request中获取地址栏后的参数 */
        const val ONL_REP_URL_PARAM_STR: String = "onlRepUrlParamStr"
    }
}
