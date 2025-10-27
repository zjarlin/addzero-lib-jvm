//package com.zlj.iot.config;
//
//import com.zlj.common.utils.reflect.FieldUtils;
//import com.zlj.common.utils.reflect.ReflectUtils;
//import com.zlj.iot.annotation.TDengineTable;
//import com.zlj.iot.domain.BaseTDengineEntity;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.lang.reflect.Field;
//import java.sql.*;
//import java.util.Date;
//import java.util.*;
//
///**
// * TDengine 模板类
// * 负责数据库初始化、超级表管理、连接管理等
// */
//@Component
//public class TDengineTemplate {
//
//    private static final Logger logger = LoggerFactory.getLogger(TDengineTemplate.class);
//
//    @Value("${timeseries.tdengine.url:jdbc:TAOS-RS://192.168.1.148:6041/information_schema}")
//    private String tdengineUrl;
//
//    @Value("${timeseries.tdengine.username:root}")
//    private String username;
//
//    @Value("${timeseries.tdengine.password:taosdata}")
//    private String password;
//
//    @Value("${timeseries.tdengine.database:iot_data}")
//    private String database;
//
//    @Value("${timeseries.tdengine.keep:365}")
//    private int keepDays;
//
//    @Value("${timeseries.tdengine.days:10}")
//    private int days;
//
//    @Value("${timeseries.tdengine.replicas:1}")
//    private int replicas;
//
//    @Value("${timeseries.tdengine.connections:10}")
//    private int maxConnections;
//
//    @Value("${timeseries.tdengine.driver-class-name}")
//    private String driverClassName;
//
//    private Connection connection;
//    private boolean initialized = false;
//
//    // 存储已初始化的超级表
//    private final Set<String> initializedSuperTables = new HashSet<>();
//
//    /**
//     * 初始化TDengine连接和数据库
//     */
//    @PostConstruct
//    public synchronized void init() {
//        if (initialized) {
//            return;
//        }
//
//        try {
//            // 1. 建立连接
//            createConnection();
//
//            // 2. 检查并创建数据库
//            createDatabaseIfNotExists();
//
//            // 3. 切换到目标数据库
//            switchDatabase();
//
//            initialized = true;
//            logger.info("TDengine模板初始化完成 - 数据库: {}", database);
//        } catch (Exception e) {
//            logger.error("TDengine模板初始化失败", e);
//            throw new RuntimeException("TDengine初始化失败", e);
//        }
//    }
//
//    /**
//     * 创建数据库连接
//     */
//    private void createConnection() throws SQLException {
//        Properties connProps = new Properties();
//        connProps.setProperty("user", username);
//        connProps.setProperty("password", password);
//        connProps.setProperty("charset", "UTF-8");
//        connProps.setProperty("locale", "en_US.UTF-8");
//        connProps.setProperty("timezone", "UTC-8");
//
//        connection = DriverManager.getConnection(tdengineUrl, connProps);
//        logger.info("TDengine连接创建成功: {}", tdengineUrl);
//    }
//
//    /**
//     * 检查并创建数据库
//     */
//    public void createDatabaseIfNotExists() {
//        try {
//            // 检查数据库是否存在
//            String checkDbSql = "SELECT 1 FROM information_schema.ins_databases WHERE name = ?";
//            try (PreparedStatement checkStmt = connection.prepareStatement(checkDbSql)) {
//                checkStmt.setString(1, database);
//                ResultSet rs = checkStmt.executeQuery();
//
//                if (!rs.next()) {
//                    // 数据库不存在，创建数据库
//                    createDatabase();
//                    logger.info("创建数据库成功: {}", database);
//                } else {
//                    logger.info("数据库已存在: {}", database);
//                }
//            }
//        } catch (SQLException e) {
//            logger.error("检查/创建数据库失败", e);
//            throw new RuntimeException("数据库操作失败", e);
//        }
//    }
//
//    /**
//     * 创建数据库
//     */
//    private void createDatabase() throws SQLException {
//        String createDbSql = String.format(
//                "CREATE DATABASE IF NOT EXISTS %s KEEP %d DURATION %d REPLICA %d",
//                database, keepDays, days, replicas
//        );
//
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute(createDbSql);
//        }
//    }
//
//    /**
//     * 切换到目标数据库
//     */
//    private void switchDatabase() throws SQLException {
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("use " + database);
//        }
//    }
//
//    /**
//     * 初始化超级表（基于实体类注解）
//     * @param entityClass 实体类
//     */
//    public <T extends BaseTDengineEntity> void initSuperTable(Class<T> entityClass) {
//        TDengineTable annotation = entityClass.getAnnotation(TDengineTable.class);
//        if (annotation == null) {
//            logger.warn("实体类 {} 未添加 @TDengineTable 注解", entityClass.getSimpleName());
//            return;
//        }
//
//        String superTableName = annotation.superTable();
//        if (initializedSuperTables.contains(superTableName)) {
//            logger.debug("超级表已初始化: {}", superTableName);
//            return;
//        }
//
//        try {
//            // 构建超级表创建SQL
//            String createSql = buildSuperTableSQL(entityClass, annotation);
//            executeUpdate(createSql);
//
//            initializedSuperTables.add(superTableName);
//            logger.info("超级表初始化成功: {}", superTableName);
//        } catch (Exception e) {
//            logger.error("初始化超级表失败: {}", superTableName, e);
//            throw new RuntimeException("超级表初始化失败: " + superTableName, e);
//        }
//    }
//
//    /**
//     * 构建超级表创建SQL
//     */
//    private <T> String buildSuperTableSQL(Class<T> entityClass, TDengineTable annotation) {
//        StringBuilder columns = new StringBuilder();
//        StringBuilder tags = new StringBuilder();
//
//        // 解析字段和标签
//        parseEntityFields(entityClass, columns, tags);
//
//        // 如果没有解析到标签，使用注解中的标签
//        if (tags.length() == 0 && !annotation.tags().isEmpty()) {
//            tags.append(annotation.tags());
//        }
//
//        String superTableName = annotation.superTable();
//        return String.format("CREATE STABLE IF NOT EXISTS %s (%s) TAGS (%s)",
//                superTableName, columns.toString(), tags.toString());
//    }
//
//    /**
//     * 解析实体类字段
//     */
//    private <T> void parseEntityFields(Class<T> entityClass, StringBuilder columns, StringBuilder tags) {
//        List<Field> fields = FieldUtils.getAllFields(entityClass);
//        List<String> columnList = new ArrayList<>();
//        List<String> tagList = new ArrayList<>();
//
//        // 默认时间戳字段
//        //columnList.add("ts TIMESTAMP");
//        for (Field field : fields) {
//            String fieldName = field.getName();
//            Class<?> fieldType = field.getType();
//
//            // 跳过序列化相关字段
//            if (fieldName.equals("serialVersionUID")) {
//                continue;
//            }
//
//            String tdengineType = getTDengineType(fieldType);
//            if (tdengineType != null) {
//                if (isTagField(fieldName)) {
//                    tagList.add(fieldName + " " + tdengineType);
//                } else {
//                    columnList.add(fieldName + " " + tdengineType);
//                }
//            }
//        }
//
//        // 构建columns和tags字符串
//        if(columnList!=null && columnList.size()>0){
//            int tsIndex = 0;
//            String tsValue = null,firstListElement=columnList.get(0);
//            if("ts".equals(firstListElement)){
//
//            }else {
//                for (int i=0;i<columnList.size();i++) {
//                    if("ts TIMESTAMP".equals(columnList.get(i))){
//                        tsValue = columnList.get(i);
//                        tsIndex = i;
//                        break;
//                    }
//                }
//                columnList.set(0,tsValue);
//                columnList.set(tsIndex,firstListElement);
//            }
//        }
//        columns.append(String.join(", ", columnList));
//        if (!tagList.isEmpty()) {
//            tags.append(String.join(", ", tagList));
//        }
//    }
//
//    /**
//     * 判断字段是否为标签字段
//     */
//    private boolean isTagField(String fieldName) {
//        // 根据字段名判断是否为标签字段
//        // 这里可以根据实际需求调整判断逻辑
//        return
//                fieldName.equals("productId") ||
//                fieldName.equals("deviceId") ||
//                fieldName.equals("pointName") ||
//                fieldName.equals("dataType");
//    }
//
//    /**
//     * 获取Java类型对应的TDengine类型
//     */
//    private String getTDengineType(Class<?> fieldType) {
//        if (fieldType == String.class) {
//            return "NCHAR(100)";
//        } else if (fieldType == Double.class || fieldType == double.class) {
//            return "DOUBLE";
//        } else if (fieldType == Float.class || fieldType == float.class) {
//            return "FLOAT";
//        } else if (fieldType == Integer.class || fieldType == int.class) {
//            return "INT";
//        } else if (fieldType == Long.class || fieldType == long.class) {
//            return "BIGINT";
//        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
//            return "BOOL";
//        } else if (fieldType == Date.class || fieldType == Timestamp.class) {
//            return "TIMESTAMP";
//        } else if (fieldType == Short.class || fieldType == short.class) {
//            return "SMALLINT";
//        } else if (fieldType == Byte.class || fieldType == byte.class) {
//            return "TINYINT";
//        }
//        return null;
//    }
//
//    /**
//     * 检查超级表是否存在
//     * @param superTableName 超级表名称
//     * @return 是否存在
//     */
//    public boolean isSuperTableExists(String superTableName) {
//        String sql = "SELECT 1 FROM information_schema.ins_stables WHERE db_name = ? AND stable_name = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, database);
//            stmt.setString(2, superTableName);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        } catch (SQLException e) {
//            logger.error("检查超级表是否存在失败: {}", superTableName, e);
//            return false;
//        }
//    }
//
//    /**
//     * 检查子表是否存在
//     * @param subTableName 子表名称
//     * @return 是否存在
//     */
//    public boolean isSubTableExists(String subTableName) {
//        String sql = "SELECT 1 FROM information_schema.ins_tables WHERE db_name = ? AND table_name = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, database);
//            stmt.setString(2, subTableName);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        } catch (SQLException e) {
//            logger.error("检查子表是否存在失败: {}", subTableName, e);
//            return false;
//        }
//    }
//
//    /**
//     * 创建子表
//     * @param subTableName 子表名称
//     * @param superTableName 超级表名称
//     * @param tags 标签值
//     * @return 是否创建成功
//     */
//    public boolean createSubTable(String subTableName, String superTableName, Map<String, Object> tags) {
//        if (isSubTableExists(subTableName)) {
//            logger.debug("子表已存在: {}", subTableName);
//            return true;
//        }
//
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("CREATE TABLE IF NOT EXISTS ").append(subTableName)
//                    .append(" USING ").append(superTableName)
//                    .append(" TAGS (");
//
//            // 构建标签值
//            List<Object> tagValues = new ArrayList<>();
//            for (Map.Entry<String, Object> entry : tags.entrySet()) {
//                tagValues.add(entry.getValue());
//            }
//
//            // 添加占位符
//            for (int i = 0; i < tagValues.size(); i++) {
//                if (i > 0) {
//                    sql.append(", ");
//                }
//                sql.append("?");
//            }
//            sql.append(")");
//
//            try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
//                for (int i = 0; i < tagValues.size(); i++) {
//                    stmt.setObject(i + 1, tagValues.get(i));
//                }
//                stmt.execute();
//            }
//
//            logger.info("创建子表成功: {}", subTableName);
//            return true;
//        } catch (SQLException e) {
//            logger.error("创建子表失败: {}", subTableName, e);
//            return false;
//        }
//    }
//
//    /**
//     * 插入数据（自动创建子表）
//     * @param entity 实体对象
//     * @return 是否插入成功
//     */
//    public <T extends BaseTDengineEntity> boolean insertData(T entity) {
//        try {
//            String subTableName = entity.getSubTableName();
//            String superTableName = entity.getSuperTableName();
//
//            // 确保子表存在
//            if (!isSubTableExists(subTableName)) {
//                Map<String, Object> tags = extractTagsFromEntity(entity);
//                if (!createSubTable(subTableName, superTableName, tags)) {
//                    return false;
//                }
//            }
//
//            // 构建插入SQL
//            String insertSql = buildInsertSQL(entity);
//            return executeUpdate(insertSql) > 0;
//        } catch (Exception e) {
//            logger.error("插入数据失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * 从实体中提取标签值
//     */
//    private <T extends BaseTDengineEntity> Map<String, Object> extractTagsFromEntity(T entity) {
//        Map<String, Object> tags = new HashMap<>();
//        TDengineTable annotation = entity.getClass().getAnnotation(TDengineTable.class);
//        if(annotation!=null && !annotation.tags().isEmpty()) {
//            String[] tagArr = annotation.tags().split(",");
//            for (String tag : tagArr) {
//                StringBuffer sb = new StringBuffer();
//                if (tag.contains("_")) {
//                    //将_去掉，并且_后面第一个字母转为大写
//                    String[] fields = tag.split("_");
//                    sb.append(fields[0]);
//                    if (fields.length > 1) {
//                        for (int i = 1; i < fields.length; i++) {
//                            String start = fields[i].substring(0, 1).toUpperCase();
//                            String end = fields[i].substring(1);
//                            sb.append(start + end);
//                        }
//                    }
//                } else {
//                    sb.append(tag);
//                }
//                tags.put(tag, ReflectUtils.getFieldValue(entity, sb.toString()));
//            }
//        }
//
//        return tags;
//    }
//
//    /**
//     * 构建插入SQL
//     */
//    private <T extends BaseTDengineEntity> String buildInsertSQL(T entity) {
//        StringBuilder sql = new StringBuilder();
//        StringBuilder values = new StringBuilder();
//
//        sql.append("INSERT INTO ").append(entity.getSubTableName())
//                .append(" USING ").append(entity.getSuperTableName())
//                .append(" TAGS (");
//
//        // 添加标签值
//        Map<String, Object> tags = extractTagsFromEntity(entity);
//        List<Object> tagValues = new ArrayList<>(tags.values());
//        for (int i = 0; i < tagValues.size(); i++) {
//            if (i > 0) {
//                sql.append(", ");
//            }
//            sql.append("'").append(tagValues.get(i)).append("'");
//        }
//        sql.append(") VALUES (");
//
//        // 添加时间戳
//        if (entity.getTs() != null) {
//            values.append("'").append(new Timestamp(entity.getTs().getTime())).append("'");
//        } else {
//            values.append("NOW");
//        }
//
//        // 添加字段值
//        Field[] fields = entity.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            if (!isTagField(field.getName()) && !field.getName().equals("ts")) {
//                try {
//                    field.setAccessible(true);
//                    Object value = field.get(entity);
//                    values.append(", ");
//
//                    if (value instanceof String) {
//                        values.append("'").append(value).append("'");
//                    } else if (value instanceof Date) {
//                        values.append("'").append(new Timestamp(((Date) value).getTime())).append("'");
//                    } else {
//                        values.append(value);
//                    }
//                } catch (IllegalAccessException e) {
//                    logger.warn("获取字段值失败: {}", field.getName(), e);
//                }
//            }
//        }
//
//        sql.append(values).append(")");
//        return sql.toString();
//    }
//
//    /**
//     * 批量插入数据
//     * @param entities 实体列表
//     * @return 是否插入成功
//     */
//    public <T extends BaseTDengineEntity> boolean batchInsertData(List<T> entities) {
//        if (entities == null || entities.isEmpty()) {
//            return true;
//        }
//
//        try {
//            StringBuilder batchSql = new StringBuilder();
//
//            for (T entity : entities) {
//                String subTableName = entity.getSubTableName();
//                String superTableName = entity.getSuperTableName();
//
//                // 确保子表存在
//                if (!isSubTableExists(subTableName)) {
//                    Map<String, Object> tags = extractTagsFromEntity(entity);
//                    if (!createSubTable(subTableName, superTableName, tags)) {
//                        continue; // 跳过创建失败的表
//                    }
//                }
//
//                // 构建插入语句
//                String insertSql = buildInsertSQL(entity);
//                batchSql.append(insertSql).append(" ");
//            }
//
//            if (batchSql.length() > 0) {
//                return executeUpdate(batchSql.toString()) > 0;
//            }
//
//            return false;
//        } catch (Exception e) {
//            logger.error("批量插入数据失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * 执行查询
//     * @param sql 查询SQL
//     * @return 结果集
//     */
//    public List<Map<String, Object>> executeQuery(String sql) {
//        List<Map<String, Object>> resultList = new ArrayList<>();
//
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            ResultSetMetaData metaData = rs.getMetaData();
//            int columnCount = metaData.getColumnCount();
//
//            while (rs.next()) {
//                Map<String, Object> row = new LinkedHashMap<>();
//                for (int i = 1; i <= columnCount; i++) {
//                    String columnName = metaData.getColumnName(i);
//                    Object value = rs.getObject(i);
//                    row.put(columnName, value);
//                }
//                resultList.add(row);
//            }
//
//            logger.debug("查询执行成功, 返回 {} 条记录", resultList.size());
//            return resultList;
//        } catch (SQLException e) {
//            logger.error("执行查询失败: {}", sql, e);
//            throw new RuntimeException("查询执行失败", e);
//        }
//    }
//
//    /**
//     * 执行更新操作
//     * @param sql SQL语句
//     * @return 影响的行数
//     */
//    public int executeUpdate(String sql) {
//        try (Statement stmt = connection.createStatement()) {
//            int affectedRows = stmt.executeUpdate(sql);
//            logger.debug("SQL执行成功: {}, 影响行数: {}", sql, affectedRows);
//            return affectedRows;
//        } catch (SQLException e) {
//            logger.error("执行SQL失败: {}", sql, e);
//            throw new RuntimeException("SQL执行失败: " + sql, e);
//        }
//    }
//
//    /**
//     * 获取数据库信息
//     * @return 数据库信息
//     */
//    public Map<String, Object> getDatabaseInfo() {
//        String sql = "SELECT * FROM information_schema.ins_databases WHERE name = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, database);
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                Map<String, Object> info = new HashMap<>();
//                ResultSetMetaData metaData = rs.getMetaData();
//                int columnCount = metaData.getColumnCount();
//
//                for (int i = 1; i <= columnCount; i++) {
//                    String columnName = metaData.getColumnName(i);
//                    Object value = rs.getObject(i);
//                    info.put(columnName, value);
//                }
//
//                return info;
//            }
//
//            return Collections.emptyMap();
//        } catch (SQLException e) {
//            logger.error("获取数据库信息失败", e);
//            return Collections.emptyMap();
//        }
//    }
//
//    /**
//     * 获取超级表列表
//     * @return 超级表列表
//     */
//    public List<String> getSuperTables() {
//        String sql = "SELECT stable_name FROM information_schema.ins_stables WHERE db_name = ?";
//        List<String> superTables = new ArrayList<>();
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, database);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                superTables.add(rs.getString("stable_name"));
//            }
//
//            return superTables;
//        } catch (SQLException e) {
//            logger.error("获取超级表列表失败", e);
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * 获取子表列表
//     * @param superTableName 超级表名称
//     * @return 子表列表
//     */
//    public List<String> getSubTables(String superTableName) {
//        String sql = "SELECT table_name FROM information_schema.ins_tables WHERE db_name = ? AND stable_name = ?";
//        List<String> subTables = new ArrayList<>();
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, database);
//            stmt.setString(2, superTableName);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                subTables.add(rs.getString("table_name"));
//            }
//
//            return subTables;
//        } catch (SQLException e) {
//            logger.error("获取子表列表失败: {}", superTableName, e);
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * 删除超级表
//     * @param superTableName 超级表名称
//     * @return 是否删除成功
//     */
//    public boolean dropSuperTable(String superTableName) {
//        String sql = "DROP STABLE IF EXISTS " + superTableName;
//
//        try {
//            executeUpdate(sql);
//            initializedSuperTables.remove(superTableName);
//            logger.info("删除超级表成功: {}", superTableName);
//            return true;
//        } catch (Exception e) {
//            logger.error("删除超级表失败: {}", superTableName, e);
//            return false;
//        }
//    }
//
//    /**
//     * 删除子表
//     * @param subTableName 子表名称
//     * @return 是否删除成功
//     */
//    public boolean dropSubTable(String subTableName) {
//        String sql = "DROP TABLE IF EXISTS " + subTableName;
//
//        try {
//            executeUpdate(sql);
//            logger.info("删除子表成功: {}", subTableName);
//            return true;
//        } catch (Exception e) {
//            logger.error("删除子表失败: {}", subTableName, e);
//            return false;
//        }
//    }
//
//    /**
//     * 获取数据库连接
//     * @return 数据库连接
//     */
//    public Connection getConnection() {
//        return connection;
//    }
//
//    /**
//     * 检查连接是否有效
//     * @return 是否有效
//     */
//    public boolean isConnectionValid() {
//        try {
//            return connection != null && !connection.isClosed() && connection.isValid(5);
//        } catch (SQLException e) {
//            logger.error("检查连接有效性失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * 重新连接
//     */
//    public synchronized void reconnect() {
//        try {
//            if (connection != null && !connection.isClosed()) {
//                connection.close();
//            }
//
//            createConnection();
//            switchDatabase();
//            logger.info("TDengine重新连接成功");
//        } catch (SQLException e) {
//            logger.error("TDengine重新连接失败", e);
//            throw new RuntimeException("重新连接失败", e);
//        }
//    }
//
//    /**
//     * 关闭连接
//     */
//    public synchronized void close() {
//        if (connection != null) {
//            try {
//                connection.close();
//                initialized = false;
//                initializedSuperTables.clear();
//                logger.info("TDengine连接已关闭");
//            } catch (SQLException e) {
//                logger.error("关闭TDengine连接失败", e);
//            }
//        }
//    }
//
//    /**
//     * 健康检查
//     * @return 健康状态
//     */
//    public Map<String, Object> healthCheck() {
//        Map<String, Object> health = new HashMap<>();
//
//        try {
//            // 检查连接
//            boolean connectionValid = isConnectionValid();
//            health.put("connection", connectionValid);
//
//            if (connectionValid) {
//                // 检查数据库
//                health.put("database", database);
//
//                // 检查超级表数量
//                List<String> superTables = getSuperTables();
//                health.put("superTables", superTables.size());
//
//                // 执行简单查询测试
//                long startTime = System.currentTimeMillis();
//                executeQuery("SELECT COUNT(*) FROM information_schema.ins_databases");
//                long responseTime = System.currentTimeMillis() - startTime;
//                health.put("responseTime", responseTime + "ms");
//
//                health.put("status", "UP");
//            } else {
//                health.put("status", "DOWN");
//            }
//        } catch (Exception e) {
//            health.put("status", "DOWN");
//            health.put("error", e.getMessage());
//        }
//
//        return health;
//    }
//
//    // Getter方法
//    public String getDatabase() {
//        return database;
//    }
//
//    public boolean isInitialized() {
//        return initialized;
//    }
//
//    public Set<String> getInitializedSuperTables() {
//        return Collections.unmodifiableSet(initializedSuperTables);
//    }
//}
