@file:JvmName("TycApis")

package site.addzero.network.call.tyc.utils

import com.alibaba.fastjson2.parseObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.tyc.constant.UrlConstant
import site.addzero.network.call.tyc.entity.detail.CompanyDetailData
import site.addzero.network.call.tyc.entity.detail.CompanyInfoRes
import site.addzero.network.call.tyc.entity.search.SearchRes
import java.net.URLEncoder

class TycApi (private val authorization: String? = null , private val authToken: String? = null){

    private val client = OkHttpClient()
    private val headers by lazy {
        val auth = authorization ?: System.getenv("TYC_AUTHORIZATION")
            ?: throw IllegalArgumentException("Authorization not provided and TYC_AUTHORIZATION environment variable not found")
        val token = authToken ?: System.getenv("TYC_X_AUTH_TOKEN")
            ?: throw IllegalArgumentException("Auth token not provided and TYC_X_AUTH_TOKEN environment variable not found")
        HeaderUtils.getHeaders(auth, token).filterKeys { it != "Accept-Encoding" }
    }

    /**
     * 获取企业基本信息
     * @param companyId 公司ID
     * @return BaseInfo 企业基本信息
     */
    fun getBaseInfo(companyId: Long?): CompanyDetailData? {
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
        return data
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
        pageNum: String = "1",
        pageSize: String = "10",
        sortType: String = "0",
    ): site.addzero.network.call.tyc.entity.search.CompanyData? {
        if (companyName.isNullOrBlank()) {
            return null
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
            return null
        }

        val responseBody = response.body?.string() ?: return null
//        val parseObject = responseBody.parseObject<site.addzero.network.call.tianyancha.domain.search.JsonRootBean>()
        val parseObject = responseBody.parseObject<SearchRes>()

//        val searches = parseObject.data?.companyList?.filterNotNull()?.map { it.toSearch() } ?: emptyList()
        val data = parseObject.data
        return data

    }


    // 私有辅助方法
    private fun buildRequest(url: String): Request {
        val builder = Request.Builder().url(url)
        headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        return builder.build()
    }

}
