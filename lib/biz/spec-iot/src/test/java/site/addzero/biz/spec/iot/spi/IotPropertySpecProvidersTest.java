package site.addzero.biz.spec.iot.spi;

import org.junit.jupiter.api.Test;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.test.spi.DuplicatePropertySpecProvider;
import site.addzero.biz.spec.iot.test.spi.TestPropertySpecProvider;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IotPropertySpecProvidersTest {

    @Test
    void shouldLoadProviderFromServiceLoader() throws Exception {
        Path tempDir = Files.createTempDirectory("spec-iot-provider");
        writeService(tempDir, Arrays.asList(TestPropertySpecProvider.class.getName()));

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, original);
        try {
            Thread.currentThread().setContextClassLoader(loader);
            IotPropertySpecProviders.reload();

            IotPropertySpecProvider provider = IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"));
            assertEquals("test-provider", provider.getName());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            IotPropertySpecProviders.reload();
            loader.close();
        }
    }

    @Test
    void shouldFailWhenDuplicateProvidersMatchSameThing() throws Exception {
        Path tempDir = Files.createTempDirectory("spec-iot-provider-duplicate");
        writeService(tempDir, Arrays.asList(
                TestPropertySpecProvider.class.getName(),
                DuplicatePropertySpecProvider.class.getName()
        ));

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, original);
        try {
            Thread.currentThread().setContextClassLoader(loader);
            IotPropertySpecProviders.reload();

            assertThrows(
                    IllegalStateException.class,
                    () -> IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"))
            );
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            IotPropertySpecProviders.reload();
            loader.close();
        }
    }

    @Test
    void shouldFailWhenNoProviderMatches() throws Exception {
        Path tempDir = Files.createTempDirectory("spec-iot-provider-empty");
        writeService(tempDir, Collections.<String>emptyList());

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, original);
        try {
            Thread.currentThread().setContextClassLoader(loader);
            IotPropertySpecProviders.reload();

            assertThrows(
                    IllegalStateException.class,
                    () -> IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"))
            );
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            IotPropertySpecProviders.reload();
            loader.close();
        }
    }

    private static void writeService(Path root, Iterable<String> providers) throws Exception {
        Path file = root.resolve("META-INF/services/site.addzero.biz.spec.iot.spi.IotPropertySpecProvider");
        Files.createDirectories(file.getParent());
        Files.write(file, providers, StandardCharsets.UTF_8);
    }
}
