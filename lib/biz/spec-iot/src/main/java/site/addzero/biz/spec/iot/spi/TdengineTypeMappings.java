package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.tdengine.TdColumnSpec;
import site.addzero.biz.spec.iot.tdengine.TdengineSchemaDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ServiceLoader facade for {@link TdengineTypeMappingProvider}.
 */
public final class TdengineTypeMappings {

    private static final TdengineTypeMappingProvider DEFAULT = new DefaultTdengineTypeMappingProvider();
    private static volatile List<TdengineTypeMappingProvider> cachedProviders;

    private TdengineTypeMappings() {
    }

    public static TdColumnSpec toColumnSpec(IotPropertySpec propertySpec) {
        return load(propertySpec.getValueType()).toColumnSpec(propertySpec);
    }

    public static TdengineTypeMappingProvider load(IotValueType valueType) {
        List<TdengineTypeMappingProvider> matches = new ArrayList<TdengineTypeMappingProvider>();
        for (TdengineTypeMappingProvider provider : providers()) {
            if (provider.supports(valueType)) {
                matches.add(provider);
            }
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple TdengineTypeMappingProviders found for value type " + valueType + ": " + providerNames(matches));
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return DEFAULT;
    }

    public static List<TdengineTypeMappingProvider> loadAll() {
        List<TdengineTypeMappingProvider> all = new ArrayList<TdengineTypeMappingProvider>(providers());
        all.add(DEFAULT);
        return Collections.unmodifiableList(all);
    }

    public static List<TdengineTypeMappingProvider> reload() {
        synchronized (TdengineTypeMappings.class) {
            cachedProviders = discoverProviders();
            return loadAll();
        }
    }

    private static List<TdengineTypeMappingProvider> providers() {
        List<TdengineTypeMappingProvider> current = cachedProviders;
        if (current != null) {
            return current;
        }
        synchronized (TdengineTypeMappings.class) {
            List<TdengineTypeMappingProvider> reloaded = cachedProviders;
            if (reloaded != null) {
                return reloaded;
            }
            cachedProviders = discoverProviders();
            return cachedProviders;
        }
    }

    private static List<TdengineTypeMappingProvider> discoverProviders() {
        ServiceLoader<TdengineTypeMappingProvider> loader = ServiceLoader.load(
                TdengineTypeMappingProvider.class,
                Thread.currentThread().getContextClassLoader()
        );
        List<TdengineTypeMappingProvider> discovered = new ArrayList<TdengineTypeMappingProvider>();
        for (TdengineTypeMappingProvider provider : loader) {
            discovered.add(provider);
        }
        return Collections.unmodifiableList(discovered);
    }

    private static String providerNames(List<TdengineTypeMappingProvider> providers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < providers.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(providers.get(i).getName());
        }
        return builder.toString();
    }

    private static final class DefaultTdengineTypeMappingProvider implements TdengineTypeMappingProvider {

        @Override
        public String getName() {
            return "default";
        }

        @Override
        public boolean supports(IotValueType valueType) {
            return valueType == IotValueType.BOOLEAN
                    || valueType == IotValueType.INT32
                    || valueType == IotValueType.FLOAT32;
        }

        @Override
        public TdColumnSpec toColumnSpec(IotPropertySpec propertySpec) {
            String identifier = propertySpec.getIdentifier().trim().toLowerCase();
            if (propertySpec.getValueType() == IotValueType.BOOLEAN) {
                return new TdColumnSpec(identifier, TdColumnSpec.TYPE_BOOL, null, null);
            }
            if (propertySpec.getValueType() == IotValueType.INT32) {
                return new TdColumnSpec(identifier, TdColumnSpec.TYPE_INT, null, null);
            }
            if (propertySpec.getValueType() == IotValueType.FLOAT32) {
                return new TdColumnSpec(identifier, TdColumnSpec.TYPE_FLOAT, null, null);
            }
            return new TdColumnSpec(identifier, TdColumnSpec.TYPE_VARCHAR, TdengineSchemaDefaults.getDefaultVarcharLength(), null);
        }
    }
}
