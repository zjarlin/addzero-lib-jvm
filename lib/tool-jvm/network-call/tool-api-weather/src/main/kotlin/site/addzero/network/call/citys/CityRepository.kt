package site.addzero.network.call.citys

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class CityRepository {
    private val dbPath = "jdbc:sqlite:${this::class.java.classLoader.getResource("weather.db")?.path}"

    init {
        Class.forName("org.sqlite.JDBC")
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(dbPath)
    }

    private fun resultSetToArea(rs: ResultSet): Area {
        return Area(
            id = rs.getLong("id"),
            areaCode = rs.getString("area_id"),
            areaName = rs.getString("area_name"),
            cityName = rs.getString("city_name"),
            provinceName = rs.getString("province_name"),
            countryName = "中国",
            continents = "亚洲"
        )
    }

    fun searchCities(keyword: String): List<Area> {
        val areas = mutableListOf<Area>()
        val sql = """
            SELECT * FROM citys 
            WHERE area_name LIKE ? OR city_name LIKE ? OR province_name LIKE ?
        """.trimIndent()

        getConnection().use { connection ->
            val statement = connection.prepareStatement(sql)
            statement.setString(1, "%$keyword%")
            statement.setString(2, "%$keyword%")
            statement.setString(3, "%$keyword%")
            
            statement.executeQuery().use { rs ->
                while (rs.next()) {
                    areas.add(resultSetToArea(rs))
                }
            }
        }
        
        return areas
    }
}