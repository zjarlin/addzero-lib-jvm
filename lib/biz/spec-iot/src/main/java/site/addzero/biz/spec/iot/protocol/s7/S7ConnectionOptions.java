package site.addzero.biz.spec.iot.protocol.s7;

/**
 * Connection parameters for a Siemens S7 PLC.
 */
public final class S7ConnectionOptions {

    private final String connectionId;
    private final String host;
    private final int port;
    private final String threadId;

    public S7ConnectionOptions(String connectionId, String host, int port) {
        this(connectionId, host, port, "0");
    }

    public S7ConnectionOptions(String connectionId, String host, int port, String threadId) {
        this.connectionId = requireText(connectionId, "connectionId");
        this.host = requireText(host, "host");
        this.port = port;
        this.threadId = threadId == null || threadId.trim().isEmpty() ? "0" : threadId.trim();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getThreadId() {
        return threadId;
    }

    String cacheKey() {
        return connectionId + ":" + threadId;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
