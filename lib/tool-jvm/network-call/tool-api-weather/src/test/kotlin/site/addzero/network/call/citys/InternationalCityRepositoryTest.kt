package site.addzero.network.call.citys

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class InternationalCityRepositoryTest {

    private lateinit var internationalCityRepository: InternationalCityRepository

    @BeforeEach
    fun setUp() {
        internationalCityRepository = InternationalCityRepository()
    }

    @Test
    fun `test search international cities`() {
        // 测试搜索国际城市
        val cities = internationalCityRepository.searchCities("巴拿马")
        assertNotNull(cities)
        // 巴拿马在示例数据库中应该存在
        assertTrue(cities.isNotEmpty()) { "应该能够搜索到巴拿马相关城市" }
    }

    @Test
    fun `test international city data structure`() {
        // 测试国际城市数据结构
        val cities = internationalCityRepository.searchCities("巴拿马")
        assertFalse(cities.isEmpty()) { "应该能够搜索到巴拿马相关城市" }

        val city = cities.first()
        assertNotNull(city.id) { "城市ID不应该为空" }
        assertNotNull(city.areaCode) { "城市代码不应该为空" }
        assertNotNull(city.areaName) { "城市名称不应该为空" }
        assertNotNull(city.countryName) { "国家名称不应该为空" }
        assertNotNull(city.continents) { "大洲名称不应该为空" }
    }
}