package site.addzero.biz.spec.iot.test.spi;

import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategy;

public final class CustomTelemetryNamingStrategy implements TelemetryTableNamingStrategy {

    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public boolean supports(IotThingRef schemaRef) {
        return "product".equals(schemaRef.getKind()) && "demo-product".equals(schemaRef.getId());
    }

    @Override
    public String stableTableName(IotThingRef schemaRef) {
        return "custom_stable_demo_product";
    }

    @Override
    public String subTableName(IotThingRef schemaRef, IotThingRef sourceRef) {
        return "custom_sub_" + sourceRef.getId().replace('-', '_');
    }
}
