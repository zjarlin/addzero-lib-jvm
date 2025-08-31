//package com.addzero.web.infra.jimmer.scalar_provider;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
///**
// * 数据库字节数组转Point坐标工具类
// * 支持多种数据库方言的空间数据类型转换
// */
//public class PointConversionUtil {
//
//    /**
//     * 数据库方言类型枚举
//     */
//    public enum DatabaseDialect {
//        MYSQL,
//        POSTGRESQL,
//        ORACLE,
//        SQL_SERVER,
//        SQLITE
//    }
//
//    /**
//     * Point坐标对象
//     */
//    public static class Point {
//        private final double x;
//        private final double y;
//
//        public Point(double x, double y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        public double getX() {
//            return x;
//        }
//
//        public double getY() {
//            return y;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("POINT(%f %f)", x, y);
//        }
//    }
//
//    /**
//     * 将字节数组转换为Point坐标（自动检测数据库类型）
//     */
//    public static Point bytesToPoint(byte[] bytes) {
//        if (bytes == null || bytes.length == 0) {
//            return null;
//        }
//
//        // 尝试自动检测数据库类型
//        DatabaseDialect dialect = detectDatabaseDialect(bytes);
//        return bytesToPoint(bytes, dialect);
//    }
//
//    /**
//     * 将字节数组转换为Point坐标（指定数据库类型）
//     */
//    public static Point bytesToPoint(byte[] bytes, DatabaseDialect dialect) {
//        if (bytes == null || bytes.length == 0) {
//            return null;
//        }
//
//        try {
//            switch (dialect) {
//                case MYSQL:
//                    return parseMySQLPoint(bytes);
//                case POSTGRESQL:
//                    return parsePostgreSQLPoint(bytes);
//                case ORACLE:
//                    return parseOraclePoint(bytes);
//                case SQL_SERVER:
//                    return parseSQLServerPoint(bytes);
//                case SQLITE:
//                    return parseSQLitePoint(bytes);
//                default:
//                    throw new IllegalArgumentException("Unsupported database dialect: " + dialect);
//            }
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Failed to parse point bytes for dialect: " + dialect, e);
//        }
//    }
//
//    /**
//     * 自动检测数据库方言类型
//     */
//    private static DatabaseDialect detectDatabaseDialect(byte[] bytes) {
//        if (bytes == null || bytes.length < 5) {
//            return DatabaseDialect.MYSQL; // 默认
//        }
//
//        // MySQL: 通常以特定的字节顺序标记开头
//        if (bytes[0] == 0x01 && bytes[4] == 0x01) {
//            return DatabaseDialect.MYSQL;
//        }
//
//        // PostgreSQL: 通常有特定的SRID标记
//        if (bytes.length >= 9 && (bytes[4] & 0x20) != 0) {
//            return DatabaseDialect.POSTGRESQL;
//        }
//
//        // Oracle: 特定的SDO_GTYPE值
//        if (bytes.length >= 8) {
//            ByteBuffer buffer = ByteBuffer.wrap(bytes, 4, 4);
//            int sdoGtype = buffer.getInt();
//            if (sdoGtype == 2001) { // Oracle Point的SDO_GTYPE
//                return DatabaseDialect.ORACLE;
//            }
//        }
//
//        // SQL Server: 特定的版本字节
//        if (bytes.length >= 1 && bytes[0] == 0x01) {
//            return DatabaseDialect.SQL_SERVER;
//        }
//
//        // 默认尝试MySQL解析（最常见）
//        return DatabaseDialect.MYSQL;
//    }
//
//    /**
//     * 解析MySQL的POINT数据类型
//     */
//    private static Point parseMySQLPoint(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        byte byteOrder = buffer.get();
//        if (byteOrder != 0x01) {
//            buffer.order(ByteOrder.BIG_ENDIAN);
//        }
//
//        int type = buffer.getInt();
//        if (type != 1) {
//            throw new IllegalArgumentException("Expected Point type (1), got: " + type);
//        }
//
//        double x = buffer.getDouble();
//        double y = buffer.getDouble();
//
//        return new Point(x, y);
//    }
//
//    /**
//     * 解析PostgreSQL的POINT数据类型
//     */
//    private static Point parsePostgreSQLPoint(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        byte byteOrder = buffer.get();
//        if (byteOrder != 0x01) {
//            buffer.order(ByteOrder.BIG_ENDIAN);
//        }
//
//        int type = buffer.getInt();
//        boolean hasSrid = (type & 0x20000000) != 0;
//
//        if (hasSrid) {
//            buffer.getInt(); // 跳过SRID
//        }
//
//        double x = buffer.getDouble();
//        double y = buffer.getDouble();
//
//        return new Point(x, y);
//    }
//
//    /**
//     * 解析Oracle的SDO_GEOMETRY类型
//     */
//    private static Point parseOraclePoint(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        buffer.order(ByteOrder.BIG_ENDIAN);
//
//        // Oracle SDO_GEOMETRY结构
//        int sdoGtype = buffer.getInt(); // SDO_GTYPE
//        int sdoSRID = buffer.getInt();  // SDO_SRID
//
//        // SDO_POINT信息
//        buffer.position(24); // 定位到SDO_POINT部分
//
//        double x = buffer.getDouble();
//        double y = buffer.getDouble();
//
//        return new Point(x, y);
//    }
//
//    /**
//     * 解析SQL Server的geometry类型
//     */
//    private static Point parseSQLServerPoint(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        byte version = buffer.get();
//        if (version != 0x01) {
//            throw new IllegalArgumentException("Unsupported SQL Server geometry version: " + version);
//        }
//
//        // 跳过序列号和属性
//        buffer.position(5);
//
//        int numPoints = buffer.getInt();
//        if (numPoints != 1) {
//            throw new IllegalArgumentException("Expected 1 point, got: " + numPoints);
//        }
//
//        double x = buffer.getDouble();
//        double y = buffer.getDouble();
//
//        return new Point(x, y);
//    }
//
//    /**
//     * 解析SQLite的BLOB格式
//     */
//    private static Point parseSQLitePoint(byte[] bytes) {
//        // SQLite通常使用WKB格式，与MySQL类似
//        return parseMySQLPoint(bytes);
//    }
//
//    /**
//     * 将Point转换为指定数据库方言的字节数组
//     */
//    public static byte[] pointToBytes(Point point, DatabaseDialect dialect) {
//        if (point == null) {
//            return null;
//        }
//
//        switch (dialect) {
//            case MYSQL:
//                return createMySQLPointBytes(point);
//            case POSTGRESQL:
//                return createPostgreSQLPointBytes(point);
//            case ORACLE:
//                return createOraclePointBytes(point);
//            case SQL_SERVER:
//                return createSQLServerPointBytes(point);
//            case SQLITE:
//                return createSQLitePointBytes(point);
//            default:
//                throw new IllegalArgumentException("Unsupported database dialect: " + dialect);
//        }
//    }
//
//    /**
//     * 创建MySQL格式的字节数组
//     */
//    private static byte[] createMySQLPointBytes(Point point) {
//        ByteBuffer buffer = ByteBuffer.allocate(25);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        buffer.put((byte) 0x01); // 字节顺序：小端序
//        buffer.putInt(1);        // 几何类型：Point
//        buffer.putDouble(point.getX());
//        buffer.putDouble(point.getY());
//
//        return buffer.array();
//    }
//
//    /**
//     * 创建PostgreSQL格式的字节数组
//     */
//    private static byte[] createPostgreSQLPointBytes(Point point) {
//        ByteBuffer buffer = ByteBuffer.allocate(29);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        buffer.put((byte) 0x01);         // 字节顺序：小端序
//        buffer.putInt(0x20000001);       // 类型：Point + SRID标志
//        buffer.putInt(4326);             // SRID: WGS84
//        buffer.putDouble(point.getX());
//        buffer.putDouble(point.getY());
//
//        return buffer.array();
//    }
//
//    /**
//     * 创建Oracle格式的字节数组
//     */
//    private static byte[] createOraclePointBytes(Point point) {
//        ByteBuffer buffer = ByteBuffer.allocate(48);
//        buffer.order(ByteOrder.BIG_ENDIAN);
//
//        // SDO_GTYPE: 2001表示2维点
//        buffer.putInt(2001);
//
//        // SDO_SRID: 坐标系（0表示未知）
//        buffer.putInt(0);
//
//        // SDO_POINT信息
//        buffer.putDouble(point.getX()); // X坐标
//        buffer.putDouble(point.getY()); // Y坐标
//        buffer.putDouble(0.0);          // Z坐标（默认为0）
//
//        // SDO_ELEM_INFO（3个整数）
//        buffer.putInt(1);
//        buffer.putInt(1);
//        buffer.putInt(1);
//
//        // SDO_ORDINATES（坐标数组）
//        buffer.putDouble(point.getX());
//        buffer.putDouble(point.getY());
//
//        return buffer.array();
//    }
//
//    /**
//     * 创建SQL Server格式的字节数组
//     */
//    private static byte[] createSQLServerPointBytes(Point point) {
//        ByteBuffer buffer = ByteBuffer.allocate(29);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//
//        // 头部信息
//        buffer.put((byte) 0x01); // 版本号
//        buffer.put((byte) 0x00); // 属性
//        buffer.putInt(0);        // 序列号
//        buffer.putInt(0);        // 实例号
//
//        // 坐标信息
//        buffer.putInt(1);        // 点数
//        buffer.putDouble(point.getX());
//        buffer.putDouble(point.getY());
//
//        // Z值和M值（默认为0）
//        buffer.putDouble(0.0);
//        buffer.putDouble(0.0);
//
//        return buffer.array();
//    }
//
//    /**
//     * 创建SQLite格式的字节数组
//     */
//    private static byte[] createSQLitePointBytes(Point point) {
//        // SQLite使用WKB格式，与MySQL相同
//        return createMySQLPointBytes(point);
//    }
//
//    /**
//     * 将WKT字符串转换为Point对象
//     */
//    public static Point fromWKT(String wkt) {
//        if (wkt == null || !wkt.startsWith("POINT")) {
//            throw new IllegalArgumentException("Invalid WKT format: " + wkt);
//        }
//
//        try {
//            String coords = wkt.substring(wkt.indexOf('(') + 1, wkt.indexOf(')'));
//            String[] parts = coords.split(" ");
//            if (parts.length != 2) {
//                throw new IllegalArgumentException("Invalid coordinate format: " + coords);
//            }
//
//            double x = Double.parseDouble(parts[0]);
//            double y = Double.parseDouble(parts[1]);
//
//            return new Point(x, y);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Failed to parse WKT: " + wkt, e);
//        }
//    }
//
//    /**
//     * 将Point对象转换为WKT字符串
//     */
//    public static String toWKT(Point point) {
//        if (point == null) {
//            return null;
//        }
//        return String.format("POINT(%f %f)", point.getX(), point.getY());
//    }
//
//    /**
//     * 验证字节数组是否为有效的Point数据
//     */
//    public static boolean isValidPointData(byte[] bytes, DatabaseDialect dialect) {
//        if (bytes == null || bytes.length == 0) {
//            return false;
//        }
//
//        try {
//            bytesToPoint(bytes, dialect);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}
