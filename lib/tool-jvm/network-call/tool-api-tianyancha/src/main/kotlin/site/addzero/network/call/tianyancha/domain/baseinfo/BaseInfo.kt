package site.addzero.network.call.tianyancha.domain.baseinfo



data class BaseInfo(
    //企业id
    val id: String? = null,
    //企业名
    val name: String? = null,
    // 万分制	企业评分
    val percentileScore: String? = null,
    // 人员规模
    val staffNumRange: String? = null,
    // 经营开始时间
    val fromTime: String? = null,
    // 法人类型，1 人 2 公司
    val type: String? = null,
    //股票名
    val bondName: String? = null,
    // 是否是小微企业 0不是 1是
    val isMicroEnt: String? = null,
    //股票曾用名
    val usedBondName: String? = null,
    //注册号
    val regNumber: String? = null,
    //注册资本
    val regCapital: String? = null,
    //登记机关
    val regInstitute: String? = null,
    //注册地址
    val regLocation: String? = null,
    //行业
    val industry: String? = null,
    //核准时间
    val approvedTime: String? = null,
    //参保人数
    val socialStaffNum: String? = null,
    //企业标签
    val tags: String? = null,
    //纳税人识别号
    val taxNumber: String? = null,
    //经营范围
    val businessScope: String? = null,
    //英文名
    val property3: String? = null,
    //简称
    val alias: String? = null,
    //组织机构代码
    val orgNumber: String? = null,
    //企业状态
    val regStatus: String? = null,
    //成立日期
    val estiblishTime: String? = null,
    //股票类型
    val bondType: String? = null,
    //法人
    val legalPersonName: String? = null,
    //经营结束时间
    val toTime: String? = null,
    //实收注册资金
    val actualCapital: String? = null,
    //企业类型
    val companyOrgType: String? = null,
    // 省份简称
    val base: String? = null,
    //统一社会信用代码
    val creditCode: String? = null,
    //曾用名
    val historyNames: String? = null,
    //股票号
    val bondNum: String? = null,
    //注册资本币种 人民币 美元 欧元 等（暂未使用）
    val regCapitalCurrency: String? = null,
    //实收注册资本币种 人民币 美元 欧元 等（暂未使用）
    val actualCapitalCurrency: String? = null,
    // 邮箱
    val email: String? = null,
    //网址
    val websiteList: String? = null,
    //企业联系方式
    val phoneNumber: String? = null,
    //吊销日期
    val revokeDate: String? = null,
    //吊销原因
    val revokeReason: String? = null,
    //注销日期
    val cancelDate: String? = null,
    //注销原因
    val cancelReason: String? = null
)
