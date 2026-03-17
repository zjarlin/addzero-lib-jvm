package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.IotThingRef

/**
 * One S7 block read range associated with a telemetry source.
 */
class S7ReadBlock(
    schemaRef: IotThingRef?,
    sourceRef: IotThingRef?,
    dataArea: S7DataArea?,
    val dataAreaNumber: Int,
    val startAddress: Int,
    val dataSize: Int,
) {

    val schemaRef: IotThingRef = schemaRef ?: throw IllegalArgumentException("schemaRef, sourceRef and dataArea must not be null")
    val sourceRef: IotThingRef = sourceRef ?: throw IllegalArgumentException("schemaRef, sourceRef and dataArea must not be null")
    val dataArea: S7DataArea = dataArea ?: throw IllegalArgumentException("schemaRef, sourceRef and dataArea must not be null")
}
