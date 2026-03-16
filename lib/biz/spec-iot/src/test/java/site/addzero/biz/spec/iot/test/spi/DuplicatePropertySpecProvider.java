package site.addzero.biz.spec.iot.test.spi;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.spi.IotPropertySpecProvider;

import java.util.Collections;
import java.util.List;

public final class DuplicatePropertySpecProvider implements IotPropertySpecProvider {

    @Override
    public String getName() {
        return "duplicate-provider";
    }

    @Override
    public boolean supports(IotThingRef thingRef) {
        return "product".equals(thingRef.getKind()) && "demo-product".equals(thingRef.getId());
    }

    @Override
    public List<IotPropertySpec> getPropertySpecs(IotThingRef thingRef) {
        return Collections.singletonList(
                IotPropertySpec.builder()
                        .identifier("counter")
                        .name("Counter")
                        .valueType(IotValueType.INT32)
                        .build()
        );
    }
}
