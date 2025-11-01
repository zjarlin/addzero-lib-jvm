package site.addzero.network.call.tianyancha.domain.search


/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @Author: chenyanfeng
 * @Date: 2019-08-13
 * @Time: 下午12:50
 */
data class Search(
    // 公司id
    val id: Long? = null,
    // 注册资金
    val regCapital: String? = null,
    // 公司名
    val name: String? = null,
    // 省份
    val base: String? = null,
    // 公司类型 1-公司，2-香港公司，3-社会组织，4-律所，5-事业单位，6-基金会
    val companyType: String? = null,
    // 开业时间
    val estiblishTime: String? = null,
    // 法人
    val legalPersonName: String? = null,
    // 1-公司 2-人
    val type: Int? = null
)
