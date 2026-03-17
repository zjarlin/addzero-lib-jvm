package site.addzero.biz.spec.iot.test.spi

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.spi.IotPropertySpecProvider

class DuplicatePropertySpecProvider : IotPropertySpecProvider {

    override val name: String = "duplicate-provider"

    override fun supports(thingRef: IotThingRef): Boolean {
        return thingRef.kind == "product" && thingRef.id == "demo-product"
    }

    override fun getPropertySpecs(thingRef: IotThingRef): List<IotPropertySpec> {
        return listOf(
            IotPropertySpec.builder()
                .identifier("counter")
                .name("Counter")
                .valueType(IotValueType.INT32)
                .build(),
        )
    }
}
