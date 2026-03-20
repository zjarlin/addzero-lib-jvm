package site.addzero.example.app;

import site.addzero.example.lib.AutoWhereUtil;
import site.addzero.example.lib.CompanionHolder;

public final class JavaConsumer {
    private JavaConsumer() {
    }

    public static String callUtil() {
        return AutoWhereUtil.greet("Java");
    }

    public static String callCompanion() {
        return CompanionHolder.join("A", "B");
    }
}
