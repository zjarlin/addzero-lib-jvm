package site.addzero.network.call.citys

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CityServiceTest {

    private lateinit var cityService: CityService

    @BeforeEach
    fun setUp() {
        cityService = CityService()
    }

    @Test
    fun `test search domestic cities by city name`() {
        // 测试搜索北京
        val cities = cityService.searchCities("北京", AreaType.DOMESTIC)
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "搜索北京应该返回非空结果" }
        
        // 验证结果包含北京
        val beijing = cities.find { it.areaName?.contains("北京") == true }
        assertNotNull(beijing, "搜索结果应该包含北京")
    }

    @Test
    fun `test search domestic cities by province name`() {
        // 测试搜索省份
        val cities = cityService.searchCities("内蒙古", AreaType.DOMESTIC)
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "搜索内蒙古应该返回非空结果" }
        
        // 验证结果包含内蒙古
        val innerMongolia = cities.find { it.provinceName?.contains("内蒙古") == true }
        assertNotNull(innerMongolia, "搜索结果应该包含内蒙古")
    }

    @Test
    fun `test search domestic cities by area name`() {
        // 测试搜索区域
        val cities = cityService.searchCities("朝阳", AreaType.DOMESTIC)
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "搜索朝阳应该返回非空结果" }
    }

    @Test
    fun `test search international cities`() {
        // 测试搜索国际城市
        val cities = cityService.searchCities("纽约", AreaType.INTERNATIONAL)
        assertNotNull(cities)
        // 这里不判断是否为空，因为数据库中可能没有纽约数据
    }

    @Test
    fun `test search with empty keyword`() {
        // 测试空关键字搜索
        val cities = cityService.searchCities("", AreaType.DOMESTIC)
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "空关键字搜索应该返回所有城市" }
    }

    @Test
    fun `test search with non existing keyword`() {
        // 测试搜索不存在的关键字
        val cities = cityService.searchCities("不存在的城市", AreaType.DOMESTIC)
        assertNotNull(cities)
        assertTrue(cities.isEmpty()) { "搜索不存在的城市应该返回空列表" }
    }

    @Test
    fun `test area data structure`() {
        // 测试区域数据结构
        val areas = cityService.searchCities("北京", AreaType.DOMESTIC)
        assertFalse(areas.isEmpty()) { "应该能够搜索到北京" }
        
        val area = areas.first()
        assertNotNull(area.id) { "区域ID不应该为空" }
        assertNotNull(area.areaCode) { "区域代码不应该为空" }
        assertNotNull(area.areaName) { "区域名称不应该为空" }
        assertEquals("中国", area.countryName) { "国内城市国家名应该是中国" }
        assertEquals("亚洲", area.continents) { "国内城市大洲应该是亚洲" }
    }

    @Test
    fun `test search all cities`() {
        // 测试搜索所有城市
        val cities = cityService.searchAllCities("北京")
        assertNotNull(cities)
        // 至少应该包含国内的北京
        assertTrue(cities.isNotEmpty()) { "搜索所有城市应该返回非空结果" }
    }
    
    @Test
    fun `test search cities with int area type for domestic`() {
        // 测试使用int类型的areaType参数搜索国内城市
        val cities = cityService.searchCities("北京", 1)
        assertNotNull(cities)
        assertTrue(cities.isNotEmpty()) { "使用areaType=1应该能够搜索到国内城市" }
        
        // 验证结果包含北京
        val beijing = cities.find { it.areaName?.contains("北京") == true }
        assertNotNull(beijing, "搜索结果应该包含北京")
    }
    
    @Test
    fun `test search cities with int area type for international`() {
        // 测试使用int类型的areaType参数搜索国际城市
        val cities = cityService.searchCities("巴拿马", 2)
        assertNotNull(cities)
        // 巴拿马应该在国际城市表中
        assertTrue(cities.isNotEmpty()) { "使用areaType=2应该能够搜索到国际城市" }
    }
    
    @Test
    fun `test search cities with invalid int area type`() {
        // 测试使用无效的int类型的areaType参数
        assertThrows(IllegalArgumentException::class.java) {
            cityService.searchCities("北京", 3)
        }
    }
}