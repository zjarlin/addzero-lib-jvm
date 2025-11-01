/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.baseinfo


data class Office(
    var area: String? = null,
    var total: Int = 0,
    var companyName: String? = null,
    var cid: Long = 0,
    var score: Int = 0,
    var state: String? = null
)
