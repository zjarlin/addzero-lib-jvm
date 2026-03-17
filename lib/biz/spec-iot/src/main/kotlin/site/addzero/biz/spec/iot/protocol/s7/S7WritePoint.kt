package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.requireText

/**
 * Single S7 write target.
 */
class S7WritePoint(
    propertyIdentifier: String?,
    valueType: IotValueType?,
    dataArea: S7DataArea?,
    val dataAreaNumber: Int,
    val byteOffset: Int,
    val bitOffset: Int,
    val writeValue: String?,
) {

    val propertyIdentifier: String = requireText(propertyIdentifier, "propertyIdentifier")
    val valueType: IotValueType = valueType ?: throw IllegalArgumentException("valueType and dataArea must not be null")
    val dataArea: S7DataArea = dataArea ?: throw IllegalArgumentException("valueType and dataArea must not be null")
}
