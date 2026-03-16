package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.tdengine.TdColumnSpec;

/**
 * Maps generic telemetry value types to TDengine columns.
 */
public interface TdengineTypeMappingProvider {

    String getName();

    boolean supports(IotValueType valueType);

    TdColumnSpec toColumnSpec(IotPropertySpec propertySpec);
}
