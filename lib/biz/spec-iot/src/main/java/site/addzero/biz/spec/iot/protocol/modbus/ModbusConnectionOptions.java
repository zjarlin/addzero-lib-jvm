package site.addzero.biz.spec.iot.protocol.modbus;

/**
 * Connection parameters for a Modbus TCP endpoint.
 */
public final class ModbusConnectionOptions {

    private final String connectionId;
    private final String host;
    private final int port;
    private final String threadId;

    public ModbusConnectionOptions(String connectionId, String host, int port) {
        this(connectionId, host, port, "0");
    }

    public ModbusConnectionOptions(String connectionId, String host, int port, String threadId) {
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
