/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.search


data class BrandAndAgencyList(
    var id: String? = null,
    var name: String? = null,
    var logo: String? = null,
    var intro: String? = null,
    var base: String? = null,
    var setupDate: String? = null,
    var type: Int = 0,
    var companyName: String? = null,
    var round: String? = null,
    var competingCount: Int = 0,
    var eventCount: Int = 0,
    var fundCount: Int = 0
)
