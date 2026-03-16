package site.addzero.biz.spec.iot.spi;

import site.addzero.biz.spec.iot.IotThingRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ServiceLoader facade for {@link IotPropertySpecProvider}.
 */
public final class IotPropertySpecProviders {

    private static volatile List<IotPropertySpecProvider> cachedProviders;

    private IotPropertySpecProviders() {
    }

    public static List<IotPropertySpecProvider> loadAll() {
        return providers();
    }

    public static IotPropertySpecProvider load(IotThingRef thingRef) {
        List<IotPropertySpecProvider> matches = new ArrayList<IotPropertySpecProvider>();
        for (IotPropertySpecProvider provider : providers()) {
            if (provider.supports(thingRef)) {
                matches.add(provider);
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalStateException("No IotPropertySpecProvider found for thing " + thingRef);
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple IotPropertySpecProviders found for thing " + thingRef + ": " + providerNames(matches));
        }
        return matches.get(0);
    }

    public static List<IotPropertySpecProvider> reload() {
        synchronized (IotPropertySpecProviders.class) {
            cachedProviders = discoverProviders();
            return cachedProviders;
        }
    }

    private static List<IotPropertySpecProvider> providers() {
        List<IotPropertySpecProvider> current = cachedProviders;
        if (current != null) {
            return current;
        }
        synchronized (IotPropertySpecProviders.class) {
            List<IotPropertySpecProvider> reloaded = cachedProviders;
            if (reloaded != null) {
                return reloaded;
            }
            cachedProviders = discoverProviders();
            return cachedProviders;
        }
    }

    private static List<IotPropertySpecProvider> discoverProviders() {
        ServiceLoader<IotPropertySpecProvider> loader = ServiceLoader.load(
                IotPropertySpecProvider.class,
                Thread.currentThread().getContextClassLoader()
        );
        List<IotPropertySpecProvider> discovered = new ArrayList<IotPropertySpecProvider>();
        for (IotPropertySpecProvider provider : loader) {
            discovered.add(provider);
        }
        return Collections.unmodifiableList(discovered);
    }

    private static String providerNames(List<IotPropertySpecProvider> providers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < providers.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(providers.get(i).getName());
        }
        return builder.toString();
    }
}
