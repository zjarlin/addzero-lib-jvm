package site.addzero.network.call.citys

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CityRepositoryTest {

    private lateinit var cityRepository: CityRepository

    @BeforeEach
    fun setUp() {
        cityRepository = CityRepository()
    }

    @Test
    fun `test search domestic cities`() {
        // 测试搜索国内城市
        val cities = cityRepository.searchCities("北京")
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "应该能够搜索到北京" }
    }

    @Test
    fun `test domestic city data structure`() {
        // 测试国内城市数据结构
        val cities = cityRepository.searchCities("北京")
        assertFalse(cities.isEmpty()) { "应该能够搜索到北京" }

        val city = cities.first()
        assertNotNull(city.id) { "城市ID不应该为空" }
        assertNotNull(city.areaCode) { "城市代码不应该为空" }
        assertNotNull(city.areaName) { "城市名称不应该为空" }
        assertNotNull(city.countryName) { "国家名称不应该为空" }
        assertNotNull(city.continents) { "大洲名称不应该为空" }
        assertEquals("中国", city.countryName) { "国内城市国家名应该是中国" }
        assertEquals("亚洲", city.continents) { "国内城市大洲应该是亚洲" }
    }
}