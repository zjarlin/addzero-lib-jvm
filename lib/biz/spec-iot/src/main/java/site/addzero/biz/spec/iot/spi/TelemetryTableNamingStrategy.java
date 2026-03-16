package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotThingRef;

/**
 * TDengine table naming strategy.
 */
public interface TelemetryTableNamingStrategy {

    String getName();

    boolean supports(IotThingRef schemaRef);

    String stableTableName(IotThingRef schemaRef);

    String subTableName(IotThingRef schemaRef, IotThingRef sourceRef);
}
