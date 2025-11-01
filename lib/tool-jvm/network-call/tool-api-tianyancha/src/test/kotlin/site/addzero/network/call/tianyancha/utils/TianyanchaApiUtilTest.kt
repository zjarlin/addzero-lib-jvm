package site.addzero.network.call.tianyancha.utils

import org.junit.jupiter.api.Test

class TianyanchaApiUtilTest {

    @Test
    fun `test getBaseInfo withTycApi valid response`() {
        val searchCompany = TycApi.searchCompany("中洛佳")
        println()
    }
    @Test
    fun odijasodij(): Unit {
        val lng = 3398690435
        val baseInfo = TycApi.getBaseInfo(lng)
        println(baseInfo)

    }

}
