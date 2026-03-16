package site.addzero.biz.spec.iot.test.spi;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.spi.IotPropertySpecProvider;

import java.util.Arrays;
import java.util.List;

public final class TestPropertySpecProvider implements IotPropertySpecProvider {

    @Override
    public String getName() {
        return "test-provider";
    }

    @Override
    public boolean supports(IotThingRef thingRef) {
        return "product".equals(thingRef.getKind()) && "demo-product".equals(thingRef.getId());
    }

    @Override
    public List<IotPropertySpec> getPropertySpecs(IotThingRef thingRef) {
        return Arrays.asList(
                IotPropertySpec.builder()
                        .identifier("temperature")
                        .name("Temperature")
                        .valueType(IotValueType.FLOAT32)
                        .build(),
                IotPropertySpec.builder()
                        .identifier("running")
                        .name("Running")
                        .valueType(IotValueType.BOOLEAN)
                        .build()
        );
    }
}
