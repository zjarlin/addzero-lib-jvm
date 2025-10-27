package com.addzero.util.taos

import java.util.*

object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {



        val util = TDengineUtil("jdbc:TAOS-RS://addzero.site:6041/iot_data", "root", "taosdata", true)

//        val util = TDengineUtil("jdbc:TAOS://addzero.site:6030/netuo_iot", "root", "taosdata", true)

        //        TDengineUtil util = new TDengineUtil("jdbc:TAOS://addzero.site:6041/netuo_iot", "root", "taosdata", true);
        val iotReceiptUploadDataEntity =
            IotReceiptUploadDataEntity(Date(), null, "33.3", true, "12", "1234", 12, "22.3", "30", "14", Date())
        util.insert("v1_001", iotReceiptUploadDataEntity)
        util.insertWithStable("a1_001", "netuo_device", iotReceiptUploadDataEntity, "样子将中路", "ce", "1")


        val list = util.getList("select * from  v1_001", IotReceiptUploadDataEntity::class.java)
        println(list)
    }
}
