package site.addzero.network.call.tianyancha.entity.detail

data class LegalInfo(
    val alias: Any?,
    val bossCertificate: Int,
    val cid: Long,
    val companyNum: Int,
    val companys: Any? = null,
    val coopCount: Int,
    val event: Any? = null,
    val headUrl: Any? = null,
    val hid: Long,
    val introduction: Any? = null,
    val name: String,
    val office: List<Office>,
    val officeV1: List<OfficeV1>,
    val partnerNum: Int,
    val partners: Any? = null,
    val pid: Any? = null,
    val role: Any? = null,
    val serviceCount: Int,
    val serviceType: Int,
    val typeJoin: Any? = null
)
