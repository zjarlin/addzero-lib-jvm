package site.addzero.network.call.citys

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class InternationalCityRepository {
    private val dbPath = "jdbc:sqlite:${this::class.java.classLoader.getResource("weather.db")?.path}"

    init {
        Class.forName("org.sqlite.JDBC")
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(dbPath)
    }

    private fun resultSetToArea(rs: ResultSet): Area {
        return Area(
            id = rs.getInt("_id"),
            areaCode = rs.getString("cityId"),
            areaName = rs.getString("cityName"),
            cityName = rs.getString("cityName"),
            provinceName = null,
            countryName = rs.getString("countryName"),
            continents = rs.getString("continents")
        )
    }

    fun searchCities(keyword: String): List<Area> {
        val areas = mutableListOf<Area>()
        val sql = """
            SELECT * FROM internal_citys 
            WHERE cityId LIKE ? OR cityName LIKE ? OR countryName LIKE ? OR continents LIKE ?
        """.trimIndent()

        getConnection().use { connection ->
            val statement = connection.prepareStatement(sql)
            statement.setString(1, "%$keyword%")
            statement.setString(2, "%$keyword%")
            statement.setString(3, "%$keyword%")
            statement.setString(4, "%$keyword%")
            
            statement.executeQuery().use { rs ->
                while (rs.next()) {
                    areas.add(resultSetToArea(rs))
                }
            }
        }
        
        return areas
    }
}