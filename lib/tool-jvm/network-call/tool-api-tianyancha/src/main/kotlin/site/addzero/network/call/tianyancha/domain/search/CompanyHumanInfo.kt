/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.search


data class CompanyHumanInfo(
    val graphId: Long = 0,
    val companyName: String? = null,
    val distinctNum: Int = 0,
    val resultCount: Int = 0,
    val resultList: MutableList<ResultList?>? = null,
    val totalPage: Int = 0
)
