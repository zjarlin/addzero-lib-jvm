package com.addzero.util.taos

import java.util.*

/**
 * @author guolinyuan
 */
class IotReceiptUploadDataEntity {
    /**
     * lng : 经纬度
     * lat : 经纬度
     * isCharging : 电池是否在充电
     * battery : 电池电量
     * workingTime : 连续开机时间
     * ei : 业务电流(A)
     * ep : 业务功率(W)
     * tmp : 温度
     * humidity : 湿度
     * downtime : 上次停机时间,时间戳,精确到毫秒的（本文时间戳均为精确到毫秒的）
     */
    var ts: Date? = null
    var lng: String? = null
    var lat: String? = null
    var isCharging: Boolean? = null
    var battery: String? = null
    var workingTime: String? = null
    private var ei: Int? = null
    var ep: String? = null
    var tmp: String? = null
    var humidity: String? = null
    var downtime: Date? = null

    constructor()

    constructor(
        ts: Date?,
        lng: String?,
        lat: String?,
        isCharging: Boolean?,
        battery: String?,
        workingTime: String?,
        ei: Int?,
        ep: String?,
        tmp: String?,
        humidity: String?,
        downtime: Date?
    ) {
        this.ts = ts
        this.lng = lng
        this.lat = lat
        this.isCharging = isCharging
        this.battery = battery
        this.workingTime = workingTime
        this.ei = ei
        this.ep = ep
        this.tmp = tmp
        this.humidity = humidity
        this.downtime = downtime
    }

    fun setEi(ei: Int?) {
        this.ei = ei
    }


    override fun toString(): String {
        return "cn.netuo.util.IotReceiptUploadDataEntity{" + "ts=" + ts + ", lng='" + lng + '\'' + ", lat='" + lat + '\'' + ", isCharging='" + isCharging + '\'' + ", battery='" + battery + '\'' + ", workingTime='" + workingTime + '\'' + ", ei='" + ei + '\'' + ", ep='" + ep + '\'' + ", tmp='" + tmp + '\'' + ", humidity='" + humidity + '\'' + ", downtime='" + downtime + '\'' + '}'
    }
}
