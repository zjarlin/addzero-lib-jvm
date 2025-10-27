package com.addzero.util.taos;

import java.util.Date;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
//       jdbc:TAOS-RS://192.168.1.148:6041/iot_data
        TDengineUtil util = new TDengineUtil("jdbc:TAOS://addzero.site:6030/netuo_iot", "root", "taosdata", true);
//        TDengineUtil util = new TDengineUtil("jdbc:TAOS://addzero.site:6041/netuo_iot", "root", "taosdata", true);

        IotReceiptUploadDataEntity iotReceiptUploadDataEntity = new IotReceiptUploadDataEntity(new Date(), null, "33.3", true, "12", "1234", 12, "22.3", "30", "14", new Date());
        util.insert("v1_001", iotReceiptUploadDataEntity);
        util.insertWithStable("a1_001", "netuo_device", iotReceiptUploadDataEntity, "样子将中路", "ce", "1");


        List<IotReceiptUploadDataEntity> list = util.getList("select * from  v1_001", IotReceiptUploadDataEntity.class);
        System.out.println(list);
    }
}
