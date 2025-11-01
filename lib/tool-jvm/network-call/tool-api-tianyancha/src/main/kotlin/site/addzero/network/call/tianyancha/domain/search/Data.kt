/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.search


data class Data(
    val companyHumanInfo: CompanyHumanInfo? = null,
    val companyTotalPage: Int = 0,
    val brandAndAgencyList: MutableList<BrandAndAgencyList?>? = null,
    val companyTotal: Int = 0,
    val agencyCount: Int = 0,
    val adviceQuery: String? = null,
    val companyCount: Int = 0,
    val brandCount: Int = 0,
    val modifiedQuery: String? = null,
    val humanCount: Int = 0,
    val companyList: MutableList<CompanyList?>? = null,
    val brandTotal: Int = 0,
    val companyHumanCount: Int = 0,
    val companyTotalStr: String? = null,
    val agencyTotal: Int = 0
)
