package site.addzero.web.modules.controller

import site.addzero.util.DatabaseMetadataUtil
import okhttp3.Connection
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@RestController
@RequestMapping("/sysJdbc")
class SysJdbcController (private val dataSource: DataSource){
    @GetMapping("/getJdbcMetaData")
    fun getJdbcMetaData(): Unit {
        val connection = dataSource.connection
        //todo 多平台配置去中心化
        DatabaseMetadataUtil.getTableMetaData(connection, "public", null, null)

    }
}
