package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotThingRef

/**
 * TDengine table naming strategy.
 */
interface TelemetryTableNamingStrategy {

    val name: String

    fun supports(schemaRef: IotThingRef): Boolean

    fun stableTableName(schemaRef: IotThingRef): String

    fun subTableName(schemaRef: IotThingRef, sourceRef: IotThingRef): String
}
