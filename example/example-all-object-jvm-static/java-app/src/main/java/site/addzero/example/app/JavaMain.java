package site.addzero.example.app;

public final class JavaMain {
    private JavaMain() {
    }

    public static void main(String[] args) {
        System.out.println(JavaConsumer.callUtil());
        System.out.println(JavaConsumer.callCompanion());
    }
}
