package site.addzero.network.call.tyc.utils

object EnumParser {
    // 公司类型 1-公司，2-香港公司，3-社会组织，4-律所，5-事业单位，6-基金会
    fun parseCompanyType(companyType: Int?): String? {
        val map = HashMap<Int?, String?>()
        map[1] = "公司"
        map[2] = "香港公司"
        map[3] = "社会组织"
        map[4] = "律所"
        map[5] = "事业单位"
        map[6] = "基金会"

        val s = map.get(companyType)
        return s
    }

    // 法人类型，1 人 2 公司
    fun parseType(type: Int?): String? {
        val map = HashMap<Int?, String?>()
        map[1] = "人"
        map[2] = "公司"

        val s = map.get(type)
        return s
    }

    //    isMicroEnt; // 是否是小微企业 0不是 1是
    fun parseIsMicroEnt(type: Int?): String? {
        val map = HashMap<Int?, String?>()
        map[0] = "不是"
        map[1] = "是"

        val s = map.get(type)
        return s
    }
}
