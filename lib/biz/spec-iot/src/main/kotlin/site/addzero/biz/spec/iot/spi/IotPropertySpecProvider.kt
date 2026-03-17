package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotThingRef

/**
 * Consumer-owned thing model provider.
 */
interface IotPropertySpecProvider {

    val name: String

    fun supports(thingRef: IotThingRef): Boolean

    fun getPropertySpecs(thingRef: IotThingRef): List<IotPropertySpec>
}
