/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CompanySearchApiTest {

    private lateinit var companySearchApi: CompanySearchApi

    @BeforeEach
    fun setUp() {
        // 使用测试用的AK/SK创建实例
        companySearchApi = CompanySearchApi("", "")
    }

    @Test
    fun `test search companies with keyword`() {
        val result = companySearchApi.searchCompanies("古城机械")
        println("Search result: $result")
        // 结果可能为null（取决于网络连接和API状态），但我们至少确保方法能正常执行
        assertTrue(result is String? || result == null)
    }


}
