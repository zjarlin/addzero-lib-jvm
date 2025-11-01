package site.addzero.network.call.tianyancha.entity.detail

/**
 * 法人信息实体类
 */
data class LegalInfo(
    /** 别名 */
    val alias: Any?,
    /** 老板证书 */
    val bossCertificate: Int,
    /** 公司ID */
    val cid: Long,
    /** 公司数量 */
    val companyNum: Int,
    /** 公司列表 */
    val companys: Any? = null,
    /** 合作次数 */
    val coopCount: Int,
    /** 事件信息 */
    val event: Any? = null,
    /** 头像URL */
    val headUrl: Any? = null,
    /** 法人_hid */
    val hid: Long,
    /** 介绍信息 */
    val introduction: Any? = null,
    /** 法人姓名 */
    val name: String,
    /** 办公室列表 */
    val office: List<Office>,
    /** 办公室列表V1 */
    val officeV1: List<OfficeV1>,
    /** 合伙人数量 */
    val partnerNum: Int,
    /** 合伙人信息 */
    val partners: Any? = null,
    /** PID */
    val pid: Any? = null,
    /** 角色 */
    val role: Any? = null,
    /** 服务次数 */
    val serviceCount: Int,
    /** 服务类型 */
    val serviceType: Int,
    /** 类型连接 */
    val typeJoin: Any? = null
)