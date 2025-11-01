package site.addzero.network.call.tyc.entity.search

/**
 * 公司信息实体类
 */
data class Company(
    /** 公司简称 */
    val abbr: String,
    /** 基本信息摘要 */
    val abstractsBaseInfo: String, // 河南中洛佳科技有限公司，成立于2019年，位于河南省洛阳市，是一家以从事软件和信息技术服务业为主的企业。企业注册资本1000万人民币，超过了94%的河南省同行，实缴资本447.5万人民币。
    /** 地址 */
    val address: Any?, // null
    /** 后比率 */
    val afterRatio: String,
    /** 别名 */
    val alias: String, // 中洛佳
    /** 区域代码列表 */
    val areaCodes: List<String>,
    /** 百度URL */
    val baiduUrl: Any?, // null
    /** 基础信息 */
    val base: String, // 河南
    /** 业务类型 */
    val bizType: String, // 7
    /** 债券名称 */
    val bondName: Any?, // null
    /** 债券编号 */
    val bondNum: Any?, // null
    /** 债券类型 */
    val bondType: Any?, // null
    /** 奖金分数 */
    val bonusScore: String, // 0
    /** 业务项目列表 */
    val businessItemList: Any?, // null
    /** 经营范围 */
    val businessScope: String, // 信息系统集成服务;仪器仪表销售;办公设备销售;机械设备销售;电子产品销售;通讯设备销售;通信设备销售;办公用品销售;建筑材料销售;信息安全设备销售;安防设备销售;第一类医疗器械销售;第二类医疗器械销售;软件销售;技术服务;技术开发;技术咨询;技术交流;技术转让;技术推广;网络技术服务;信息技术咨询服务;网络与信息安全软件开发;软件开发;信息系统运行维护服务;计算机系统服务;安全技术防范系统设计施工服务;交通及公共管理用金属标牌制造;交通及公共管理用标牌销售;商务代理代办服务;凭营业执照依法自主开展经营活动;建筑智能化系统设计;依托实体医院的互联网医院服务;建设工程施工
    /** 分类代码 */
    val categoryCode: String, // 656
    /** 2017年分类代码列表 */
    val categoryCode2017List: List<String>,
    /** 标准分类代码 */
    val categoryCodeStd: String, // 651
    /** 分类字符串 */
    val categoryStr: String, // 软件和信息技术服务业
    /** 变更金额 */
    val changeAmt: String,
    /** 变更比率 */
    val changeRatio: String,
    /** 变更时间 */
    val changeTime: String,
    /** 城市 */
    val city: String, // 洛阳市
    /** 声明信息 */
    val claimInfo: Any?, // null
    /** 声明包类型 */
    val claimPkgType: Any?, // null
    /** 公司品牌信息 */
    val companyBrandInfo: Any?, // null
    /** 公司集团信息 */
    val companyGroupInfo: Any?, // null
    /** 公司数量 */
    val companyNum: Any?, // null
    /** 公司组织类型 */
    val companyOrgType: String, // 有限责任公司	;	有限责任公司(自然人投资或控股)
    /** 公司电话簿 */
    val companyPhoneBook: CompanyPhoneBook,
    /** 公司问题 */
    val companyQuestions: CompanyQuestions,
    /** 公司规模 */
    val companyScale: String?, // 小型
    /** 公司评分 */
    val companyScore: String, // 73
    /** 公司类型 */
    val companyType: Int, // 1
    /** 联系人映射 */
    val contantMap: ContantMap,
    /** 信用代码 */
    val creditCode: String, // 91410307MA47WC788D
    /** 部门 */
    val department: String,
    /** 距离 */
    val distance: Any?, // null
    /** 区域 */
    val district: String, // 洛龙区
    /** 文档特征 */
    val docFeature: String, // {"isFollowed":0,"isMonitored":0,"historyClicks":0,"byAnn":0,"timeClicks":[0,0,1,1],"byBaidu":0}
    /** 邮箱列表 */
    val emailList: List<String>,
    /** 邮箱 */
    val emails: String, // hnzljkj@163.com	;	419569781@qq.com	;	41956781@qq.com	;	zlj65199909@163.com
    /** 英文名称 */
    val englishName: String?, // Henan Zhongluojia Technology Co., Ltd
    /** 成立时间 */
    val establishmentTime: String, // 20191217
    /** 成立时间 */
    val estiblishTime: String, // 2019-12-17 00:00:00.0
    /** 成立时间显示字符串 */
    val estiblishTimeShowStr: String, // 成立日期
    /** 执行人员 */
    val executive: Any?, // null
    /** 融资轮次 */
    val financingRound: String, // 未融资
    /** 首要职位显示字符串 */
    val firstPositionShowStr: String, // 法定代表人
    /** 首要职位值 */
    val firstPositionValue: String, // 马丽北
    /** 地理位置 */
    val geoLocation: Any?, // null
    /** B组ID */
    val gidForB: String, // 3398690435
    /** 是否有更多电话 */
    val hasMorePhone: Any?, // null
    /** 是否有视频 */
    val hasVideo: Any?, // null
    /** 隐藏状态 */
    val hidden: Int, // 0
    /** 隐藏电话 */
    val hiddenPhones: Any?, // null
    /** 历史名称 */
    val historyNames: String,
    /** 人员名称 */
    val humanNames: String, // 李莹莹	:#0	;	马丽北	:#0	;	马丽北	:#1	;	李莹莹	:#1	;
    /** ICP备案 */
    val icp: String,
    /** ICP备案列表 */
    val icps: Any?, // null
    /** ID */
    val id: Long, // 3398690435
    /** 违法类型 */
    val illegalType: String, // 0
    /** 行业 */
    val industry: Any?, // null
    /** 机构类型列表 */
    val institutionTypeList: List<String>,
    /** 是否为分支机构 */
    val isBranch: Int, // 0
    /** 是否已认领 */
    val isClaimed: Int, // 0
    /** 是否在内 */
    val isIn: String,
    /** 是否推荐 */
    val isRecommend: Any?, // null
    /** 标签JSON列表 */
    val labelJsonList: List<String>,
    /** 标签列表 */
    val labelList: Any?, // null
    /** 标签列表V2 */
    val labelListV2: List<String>,
    /** 纬度 */
    val latitude: Any?, // null
    /** 法人 */
    val legalPerson: String,
    /** 法人ID */
    val legalPersonId: String, // 3398690439
    /** 法人姓名 */
    val legalPersonName: String, // 马丽北
    /** 法人显示字符串 */
    val legalPersonShowStr: String, // 法定代表人
    /** 法人类型 */
    val legalPersonType: String, // 1
    /** logo */
    val logo: String,
    /** 经度 */
    val longitude: Any?, // null
    /** 主ID */
    val mainId: String,
    /** 匹配字段 */
    val matchField: MatchField?,
    /** 匹配类型 */
    val matchType: String, // 公司名称匹配
    /** 多匹配字段 */
    val multiMatchField: List<MultiMatchField>,
    /** 名称 */
    val name: String, // 河南<em>中洛佳</em>科技有限公司
    /** 新测试名称 */
    val newtestName: Any?, // null
    /** 无实际资本 */
    val noActualCapital: Any?, // null
    /** 组织机构代码 */
    val orgNumber: String, // MA47WC78-8
    /** 原始分数 */
    val orginalScore: String, // 7332
    /** 电话 */
    val phone: String, // 037965199909	;	0379-65199909
    /** 电话信息列表 */
    val phoneInfoList: List<PhoneInfo>,
    /** 电话列表 */
    val phoneList: List<String>,
    /** 电话号码 */
    val phoneNum: String, // 0379-65199909
    /** 产品列表 */
    val productList: Any?, // null
    /** 省份 */
    val province: String, // 00410311	;	00410300	;	00410000
    /** 合格金融产品 */
    val qualifiedFinancialProduct: Any?, // null
    /** 实际声明包类型 */
    val realClaimPkgType: Any?, // null
    /** 注册资本 */
    val regCapital: String, // 1000万人民币
    /** 注册资本显示字符串 */
    val regCapitalShowStr: String, // 注册资本
    /** 注册地址 */
    val regLocation: String, // 河南省洛阳市太康路与汇通街交叉口大数据产业园综合楼四楼405室
    /** 注册号 */
    val regNumber: String, // 410394000177215
    /** 注册状态 */
    val regStatus: String, // 存续
    /** 注册机构 */
    val registerInstitute: String, // 洛阳市洛龙区市场监督管理局
    /** 报告货币 */
    val repCurrency: Any?, // null
    /** 住宅楼 */
    val residentialBuilding: Any?, // null
    /** 分数 */
    val score: String, // 73
    /** 第二职位显示字符串 */
    val secondPositionShowStr: String, // 注册资本
    /** 第二职位值 */
    val secondPositionValue: String, // 1000万人民币
    /** 社保员工数量 */
    val socialSecurityStaff_num: String?, // 21
    /** 员工数量报告年份 */
    val staffNumReportYear: Int, // 2024
    /** 标签列表 */
    val tagList: Any?, // null
    /** 目标GID */
    val targetGid: String,
    /** 目标名称 */
    val targetName: String,
    /** 目标注册资本金额 */
    val targetRegCapitalAmount: String,
    /** 目标注册资本货币 */
    val targetRegCapitalCurrency: String,
    /** 税务代码 */
    val taxCode: String, // 91410307MA47WC788D
    /** 三个月诉讼 */
    val threeMonthsLawsuit: Any?, // null
    /** 商标列表 */
    val tmList: Any?, // null
    /** 商标 */
    val trademarks: Any?, // null
    /** 类型 */
    val type: Int, // 1
    /** 曾用债券名称 */
    val usedBondName: Any?, // null
    /** 视频ID */
    val videoId: Any?, // null
    /** 网站备案数量 */
    val websiteFilingCount: Int, // 0
    /** 网站 */
    val websites: String
)
