@file:JvmName("TycApis")
package site.addzero.network.call.tianyancha.utils

import com.alibaba.fastjson2.parseObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.tianyancha.constant.UrlConstant
import site.addzero.network.call.tianyancha.domain.baseinfo.BaseInfo
import site.addzero.network.call.tianyancha.domain.baseinfo.Data
import site.addzero.network.call.tianyancha.domain.search.CompanyList
import site.addzero.network.call.tianyancha.domain.search.Search
import site.addzero.network.call.tianyancha.entity.CompanyInfoRes
import java.net.URLEncoder
import java.util.*

object TycApi {
   private const val AUTHORIZATION = "0###oo34J0ZRgatN5UBO8UQRwap6Ew_A###1565664617903###24ed6f7b1512aee63869b97552a2bd8f"

    private val client = OkHttpClient()
    private val headers by lazy {
        // 移除Accept-Encoding头部，让OkHttp自动处理解压缩
        HeaderUtils.getHeaders(AUTHORIZATION).filterKeys { it != "Accept-Encoding" }
    }

    /**
     * 获取企业基本信息
     * @param companyId 公司ID
     * @return BaseInfo 企业基本信息
     */
    fun getBaseInfo(companyId: Long?): BaseInfo? {
        companyId ?: return null
        val url = UrlConstant.BASE_INFO_URL + URLEncoder.encode(companyId.toString(), "UTF-8")
        val request = buildRequest(url)
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            println("请求失败，状态码: ${response.code}")
            return null
        }
        val responseBody = response.body?.string() ?: return null
        val parseObject = responseBody.parseObject<CompanyInfoRes>()
        val data = parseObject.data
        val toBaseInfo = data?.toBaseInfo2()
        return toBaseInfo
    }

    /**
     * 搜索企业
     * @param companyName 企业名称
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param sortType 排序类型
     * @return List<Search> 搜索结果列表
     */
    fun searchCompany(
        companyName: String?,
        pageNum: String="1",
        pageSize: String="10",
        sortType: String="0",
    ): List<Search> {
        if (companyName.isNullOrBlank()) {
            return emptyList()
        }
        val baseUrlWithCompany = UrlConstant.SEARCH_RUL + URLEncoder.encode(companyName, "UTF-8")

        val url = baseUrlWithCompany.toHttpUrl().newBuilder().apply {
            addQueryParameter("pageNum", pageNum)
            addQueryParameter("pageSize", pageSize)
            addQueryParameter("sortType", sortType)
        }.build().toString()
        val request = buildRequest(url)
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("请求失败，状态码: ${response.code}")
            return emptyList()
        }

        val responseBody = response.body?.string() ?: return emptyList()
        println("响应内容: $responseBody") // 添加调试信息
        val parseObject = responseBody.parseObject<site.addzero.network.call.tianyancha.domain.search.JsonRootBean>()
        val searches = parseObject.data?.companyList?.filterNotNull()?.map { it.toSearch() } ?: emptyList()
        return searches

    }


    // 私有辅助方法
    private fun buildRequest(url: String): Request {
        val builder = Request.Builder().url(url)
        headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        return builder.build()
    }

    // 扩展函数，将Data转换为BaseInfo
//    private fun Data.toBaseInfo(): BaseInfo {
//        return BaseInfo(
//            id = this.id.toString(),
//            name = this.name,
//            percentileScore = this.percentileScore.toString(),
//            staffNumRange = this.staffNumRange,
//            fromTime = if (this.fromTime > 0) Date(this.fromTime).toString() else "",
//            type = EnumParser.parseType(this.type),
//            isMicroEnt = EnumParser.parseIsMicroEnt(this.isMicroEnt),
//            regNumber = this.regNumber,
//            regCapital = this.regCapital,
//            regInstitute = this.regInstitute,
//            regLocation = this.regLocation,
//            industry = this.industry,
//            approvedTime = if (this.approvedTime > 0) Date(this.approvedTime).toString() else "",
//            socialStaffNum = this.socialStaffNum.toString(),
//            tags = this.tags,
//            taxNumber = this.taxNumber,
//            businessScope = this.businessScope,
//            property3 = this.property3,
//            alias = this.alias,
//            orgNumber = this.orgNumber,
//            regStatus = this.regStatus,
//            estiblishTime = this.estiblishTimeTitleName,
//            legalPersonName = this.legalPersonName,
//            toTime = if (this.toTime > 0) Date(this.toTime).toString() else "",
//            actualCapital = this.actualCapital,
//            companyOrgType = this.companyOrgType,
//            base = this.base,
//            creditCode = this.creditCode,
//            email = this.email,
//            websiteList = this.websiteList,
//            phoneNumber = this.phoneNumber
//        )
//    }

    // 扩展函数，将CompanyList转换为Search
    private fun CompanyList.toSearch(): Search {
        return Search(
            id = this.id,
            regCapital = this.regCapital,
            name = EmUtils.removeEmTag(this.name),
            base = this.base,
            companyType = EnumParser.parseCompanyType(this.companyType),
            estiblishTime = this.estiblishTime,
            legalPersonName = this.legalPersonName,
            type = this.type
        )
    }
}

private fun site.addzero.network.call.tianyancha.entity.Data.toBaseInfo2(): BaseInfo {
    val data = this
    return BaseInfo(
        id = data.id.toString(),
        name = this.name,
        percentileScore = this.percentileScore.toString(),
        staffNumRange = this.staffNumRange,
        fromTime = if (this.fromTime > 0) Date(this.fromTime).toString() else "",
        type = EnumParser.parseType(this.type),
//        isMicroEnt = EnumParser.parseIsMicroEnt(this.isMicroEnt),
        regNumber = this.regNumber,
        regCapital = this.regCapital,
        regInstitute = this.regInstitute,
        regLocation = this.regLocation,
        industry = this.industry,
        approvedTime = if (this.approvedTime > 0) Date(this.approvedTime).toString() else "",
        socialStaffNum = this.socialStaffNum.toString(),
        tags = this.tags,
        taxNumber = this.taxNumber,
        businessScope = this.businessScope,
        property3 = this.property3,
        alias = this.alias,
        orgNumber = this.orgNumber,
        regStatus = this.regStatus,
        estiblishTime = this.estiblishTimeTitleName,
        legalPersonName = this.legalPersonName,
//        toTime = if (this.toTime > 0) Date(this.toTime).toString() else "",
        actualCapital = this.actualCapital,
        companyOrgType = this.companyOrgType,
        base = this.base,
        creditCode = this.creditCode,
        email = this.email,
        websiteList = this.websiteList,
        phoneNumber = this.phoneNumber
    )
}
