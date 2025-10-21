package site.addzero.util.ddlgenerator.model

/**
 * 列类型枚举
 */
enum class ColumnType {
    // 数值类型
    INT,
    BIGINT,
    SMALLINT,
    TINYINT,
    DECIMAL,
    FLOAT,
    DOUBLE,

    // 字符串类型
    VARCHAR,
    CHAR,
    TEXT,
    LONGTEXT,

    // 日期时间类型
    DATE,
    TIME,
    DATETIME,
    TIMESTAMP,

    // 布尔类型
    BOOLEAN,

    // 二进制类型
    BLOB,
    BYTES
}