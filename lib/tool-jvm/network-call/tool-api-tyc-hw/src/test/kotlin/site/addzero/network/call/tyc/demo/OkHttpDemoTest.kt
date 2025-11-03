/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.demo

import org.junit.jupiter.api.Test

class OkHttpDemoTest {

    @Test
    fun `test constructor with valid ak and sk`() {
        val companySearchApi = CompanySearchApi("44e3423356564524a36cb8a9ae1a927c", "3b1d3095ced04d44bb0c001ea7d0a5c3")
        val executeRequest = companySearchApi.executeRequest(
            "http://kzenterprisewmh.apistore.huaweicloud.com/api-mall/api/company_search/query?keyword=%E5%8F%A4%E5%9F%8E%E6%9C%BA%E6%A2%B0&pageNum=1&pageSize=10",
            "GET"
        )
        println(executeRequest)


    }


}
