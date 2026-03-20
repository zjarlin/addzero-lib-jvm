package site.addzero.example.app;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaConsumerTest {

    @Test
    void java_can_call_kotlin_object_methods_directly() {
        assertEquals("Hello, Java", JavaConsumer.callUtil());
        assertEquals("A-B", JavaConsumer.callCompanion());
    }

    @Test
    void exported_methods_are_static() throws Exception {
        Method greet = Class.forName("site.addzero.example.lib.AutoWhereUtil")
            .getDeclaredMethod("greet", String.class);
        Method join = Class.forName("site.addzero.example.lib.CompanionHolder")
            .getDeclaredMethod("join", String.class, String.class);

        assertTrue(Modifier.isStatic(greet.getModifiers()));
        assertTrue(Modifier.isStatic(join.getModifiers()));
    }
}
