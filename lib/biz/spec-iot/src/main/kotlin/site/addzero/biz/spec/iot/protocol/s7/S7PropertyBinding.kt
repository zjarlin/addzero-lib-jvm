package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.requireText

/**
 * Property-to-byte binding for S7 payload decoding.
 */
class S7PropertyBinding(
    identifier: String?,
    valueType: IotValueType?,
    val byteOffset: Int,
    val bitOffset: Int,
) {

    val identifier: String = requireText(identifier, "identifier")
    val valueType: IotValueType = valueType ?: throw IllegalArgumentException("valueType must not be null")
}
