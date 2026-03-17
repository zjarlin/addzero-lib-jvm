package site.addzero.biz.spec.iot.test.spi

import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategy

class CustomTelemetryNamingStrategy : TelemetryTableNamingStrategy {

    override val name: String = "custom"

    override fun supports(schemaRef: IotThingRef): Boolean {
        return schemaRef.kind == "product" && schemaRef.id == "demo-product"
    }

    override fun stableTableName(schemaRef: IotThingRef): String {
        return "custom_stable_demo_product"
    }

    override fun subTableName(schemaRef: IotThingRef, sourceRef: IotThingRef): String {
        return "custom_sub_" + sourceRef.id.replace('-', '_')
    }
}
