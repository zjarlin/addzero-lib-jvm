/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.search


data class ResultList(
    var name: String? = null,
    var hid: Long = 0,
    var headUrl: String? = null,
    var companyNum: Int = 0,
    var office: MutableList<Office?>? = null,
    var partners: String? = null,
    var cid: Long = 0,
    var typeJoin: String? = null,
    var alias: String? = null,
    var pid: String? = null,
    var role: String? = null
)
