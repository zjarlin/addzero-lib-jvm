package site.addzero.network.call.weatherutil

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CityDataFetcherTest {

    @Test
    fun `test parse province info`() {
        val provinceInfo = CityDataFetcher.searchCityData("洛阳")
        println(provinceInfo)


    }


}
