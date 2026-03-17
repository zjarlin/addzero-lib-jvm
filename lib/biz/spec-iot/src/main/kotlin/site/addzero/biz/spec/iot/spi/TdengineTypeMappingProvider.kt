package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.tdengine.TdColumnSpec

/**
 * Maps generic telemetry value types to TDengine columns.
 */
interface TdengineTypeMappingProvider {

    val name: String

    fun supports(valueType: IotValueType): Boolean

    fun toColumnSpec(propertySpec: IotPropertySpec): TdColumnSpec
}
