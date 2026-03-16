package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotThingRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ServiceLoader facade for {@link TelemetryTableNamingStrategy}.
 */
public final class TelemetryTableNamingStrategies {

    private static final TelemetryTableNamingStrategy DEFAULT = new DefaultTelemetryTableNamingStrategy();
    private static volatile List<TelemetryTableNamingStrategy> cachedProviders;

    private TelemetryTableNamingStrategies() {
    }

    public static TelemetryTableNamingStrategy load(IotThingRef schemaRef) {
        List<TelemetryTableNamingStrategy> matches = new ArrayList<TelemetryTableNamingStrategy>();
        for (TelemetryTableNamingStrategy strategy : providers()) {
            if (strategy.supports(schemaRef)) {
                matches.add(strategy);
            }
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple TelemetryTableNamingStrategies found for thing " + schemaRef + ": " + strategyNames(matches));
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return DEFAULT;
    }

    public static List<TelemetryTableNamingStrategy> loadAll() {
        List<TelemetryTableNamingStrategy> all = new ArrayList<TelemetryTableNamingStrategy>(providers());
        all.add(DEFAULT);
        return Collections.unmodifiableList(all);
    }

    public static List<TelemetryTableNamingStrategy> reload() {
        synchronized (TelemetryTableNamingStrategies.class) {
            cachedProviders = discoverProviders();
            return loadAll();
        }
    }

    private static List<TelemetryTableNamingStrategy> providers() {
        List<TelemetryTableNamingStrategy> current = cachedProviders;
        if (current != null) {
            return current;
        }
        synchronized (TelemetryTableNamingStrategies.class) {
            List<TelemetryTableNamingStrategy> reloaded = cachedProviders;
            if (reloaded != null) {
                return reloaded;
            }
            cachedProviders = discoverProviders();
            return cachedProviders;
        }
    }

    private static List<TelemetryTableNamingStrategy> discoverProviders() {
        ServiceLoader<TelemetryTableNamingStrategy> loader = ServiceLoader.load(
                TelemetryTableNamingStrategy.class,
                Thread.currentThread().getContextClassLoader()
        );
        List<TelemetryTableNamingStrategy> discovered = new ArrayList<TelemetryTableNamingStrategy>();
        for (TelemetryTableNamingStrategy strategy : loader) {
            discovered.add(strategy);
        }
        return Collections.unmodifiableList(discovered);
    }

    private static String strategyNames(List<TelemetryTableNamingStrategy> strategies) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strategies.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(strategies.get(i).getName());
        }
        return builder.toString();
    }

    private static final class DefaultTelemetryTableNamingStrategy implements TelemetryTableNamingStrategy {

        @Override
        public String getName() {
            return "default";
        }

        @Override
        public boolean supports(IotThingRef schemaRef) {
            return true;
        }

        @Override
        public String stableTableName(IotThingRef schemaRef) {
            return "telemetry_" + normalize(schemaRef.getKind()) + "_" + normalize(schemaRef.getId());
        }

        @Override
        public String subTableName(IotThingRef schemaRef, IotThingRef sourceRef) {
            return "telemetry_" + normalize(sourceRef.getKind()) + "_" + normalize(sourceRef.getId());
        }

        private String normalize(String value) {
            String lower = value == null ? "" : value.trim().toLowerCase();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lower.length(); i++) {
                char current = lower.charAt(i);
                if ((current >= 'a' && current <= 'z') || (current >= '0' && current <= '9') || current == '_') {
                    builder.append(current);
                } else {
                    builder.append('_');
                }
            }
            if (builder.length() == 0 || Character.isDigit(builder.charAt(0))) {
                builder.insert(0, 't');
                builder.insert(1, '_');
            }
            return builder.toString();
        }
    }
}
