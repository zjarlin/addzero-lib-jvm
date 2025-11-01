package site.addzero.network.call.tianyancha.entity

data class LegalInfo(
    val alias: Any,
    val bossCertificate: Int,
    val cid: Long,
    val companyNum: Int,
    val companys: Any,
    val coopCount: Int,
    val event: Any,
    val headUrl: Any,
    val hid: Long,
    val introduction: Any,
    val name: String,
    val office: List<Office>,
    val officeV1: List<OfficeV1>,
    val partnerNum: Int,
    val partners: Any,
    val pid: Any,
    val role: Any,
    val serviceCount: Int,
    val serviceType: Int,
    val typeJoin: Any
)
