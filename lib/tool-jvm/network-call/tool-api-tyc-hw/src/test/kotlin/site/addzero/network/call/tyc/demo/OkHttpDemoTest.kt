/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.demo

import org.junit.jupiter.api.Test
import site.addzero.network.call.tyc.api.CompanySearchApi

class OkHttpDemoTest {

    @Test
    fun `test constructor with valid ak and sk`() {
        val companySearchApi = CompanySearchApi("44e3423356564524a36cb8a9ae1a927c", "3b1d3095ced04d44bb0c001ea7d0a5c3")
        val executeRequest = companySearchApi.searchCompanies("古城机械")
        println(executeRequest)
    }
}
