package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotThingRef;

import java.util.List;

/**
 * Consumer-owned thing model provider.
 */
public interface IotPropertySpecProvider {

    String getName();

    boolean supports(IotThingRef thingRef);

    List<IotPropertySpec> getPropertySpecs(IotThingRef thingRef);
}
