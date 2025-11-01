package site.addzero.network.call.tianyancha.utils

import org.junit.jupiter.api.Test

class TianyanchaApiUtilTest {

    @Test
    fun `test getBaseInfo withTycApi valid response`() {
        val searchCompany = TycApi.searchCompany("中洛佳")
        val firstOrNull = searchCompany.map { it.id }.firstOrNull()
        val baseInfo = TycApi.getBaseInfo(firstOrNull)
//        val searchCompany1 = TianyanchaApiUtil.searchCompany("百度")
        println()
    }

}
