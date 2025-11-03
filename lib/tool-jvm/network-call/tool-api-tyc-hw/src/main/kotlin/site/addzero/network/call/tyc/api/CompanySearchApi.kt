/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.api

import com.alibaba.fastjson2.parseObject
import site.addzero.network.call.tyc.api.HuaweiCloudApi
import site.addzero.network.call.tyc.model.CompanyRes
import site.addzero.network.call.tyc.model.Data

/**
 * 企业搜索API工具类
 * 封装了天眼查企业搜索接口的调用
 */
class CompanySearchApi(ak: String, sk: String) {
    private val huaweiCloudApi = HuaweiCloudApi(ak, sk)

    /**
     * 搜索企业信息
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return API响应结果
     */
    fun searchCompanies(
        keyword: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): Data {
        val encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8")
        val url = "http://kzenterprisewmh.apistore.huaweicloud.com/api-mall/api/company_search/query" +
                "?keyword=$encodedKeyword&pageNum=$pageNum&pageSize=$pageSize"
        val executeRequest = huaweiCloudApi.executeRequest(url, "GET")
        val parseObject = executeRequest.parseObject<CompanyRes>()
        return parseObject.data
    }

}
