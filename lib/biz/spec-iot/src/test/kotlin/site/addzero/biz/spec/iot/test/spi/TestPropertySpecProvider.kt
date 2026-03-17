package site.addzero.biz.spec.iot.test.spi

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.spi.IotPropertySpecProvider

class TestPropertySpecProvider : IotPropertySpecProvider {

    override val name: String = "test-provider"

    override fun supports(thingRef: IotThingRef): Boolean {
        return thingRef.kind == "product" && thingRef.id == "demo-product"
    }

    override fun getPropertySpecs(thingRef: IotThingRef): List<IotPropertySpec> {
        return listOf(
            IotPropertySpec.builder()
                .identifier("temperature")
                .name("Temperature")
                .valueType(IotValueType.FLOAT32)
                .build(),
            IotPropertySpec.builder()
                .identifier("running")
                .name("Running")
                .valueType(IotValueType.BOOLEAN)
                .build(),
        )
    }
}
