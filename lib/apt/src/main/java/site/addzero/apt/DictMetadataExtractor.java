package site.addzero.apt;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.sql.*;
import java.util.*;

/**
 * 字典元数据抽取器
 * 
 * 从数据库中抽取字典表和字典项表的元数据
 */
public class DictMetadataExtractor {

    private final Messager messager;
    private final DictConfig config;

    public DictMetadataExtractor(Messager messager, DictConfig config) {
        this.messager = messager;
        this.config = config;
    }

    /**
     * 字典配置
     */
    public static class DictConfig {
        public final String jdbcDriver;
        public final String jdbcUrl;
        public final String jdbcUsername;
        public final String jdbcPassword;
        public final String dictTableName;
        public final String dictIdColumn;
        public final String dictCodeColumn;
        public final String dictNameColumn;
        public final String dictItemTableName;
        public final String dictItemForeignKeyColumn;
        public final String dictItemCodeColumn;
        public final String dictItemNameColumn;

        public DictConfig(
                String jdbcDriver, String jdbcUrl, String jdbcUsername, String jdbcPassword,
                String dictTableName, String dictIdColumn, String dictCodeColumn, String dictNameColumn,
                String dictItemTableName, String dictItemForeignKeyColumn,
                String dictItemCodeColumn, String dictItemNameColumn) {
            this.jdbcDriver = jdbcDriver;
            this.jdbcUrl = jdbcUrl;
            this.jdbcUsername = jdbcUsername;
            this.jdbcPassword = jdbcPassword;
            this.dictTableName = dictTableName;
            this.dictIdColumn = dictIdColumn;
            this.dictCodeColumn = dictCodeColumn;
            this.dictNameColumn = dictNameColumn;
            this.dictItemTableName = dictItemTableName;
            this.dictItemForeignKeyColumn = dictItemForeignKeyColumn;
            this.dictItemCodeColumn = dictItemCodeColumn;
            this.dictItemNameColumn = dictItemNameColumn;
        }
    }

    /**
     * 字典项数据类
     */
    public static class DictItem {
        public final String code;
        public final String desc;

        public DictItem(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 字典元数据
     */
    public static class DictMetadata {
        public final String dictCode;
        public final String dictName;
        public final List<DictItem> items;

        public DictMetadata(String dictCode, String dictName, List<DictItem> items) {
            this.dictCode = dictCode;
            this.dictName = dictName;
            this.items = items;
        }
    }

    /**
     * 从数据库提取字典元数据
     */
    public List<DictMetadata> extractDictMetadata() throws ClassNotFoundException, SQLException {
        Class.forName(config.jdbcDriver);
        
        messager.printMessage(Diagnostic.Kind.NOTE, "正在连接数据库: " + config.jdbcUrl);
        
        Properties props = new Properties();
        props.setProperty("user", config.jdbcUsername);
        props.setProperty("password", config.jdbcPassword);
        props.setProperty("connectTimeout", "5");
        
        try (Connection connection = DriverManager.getConnection(config.jdbcUrl, props)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "数据库连接成功");
            
            String sql = String.format(
                "SELECT " +
                "    d.%s as dict_id, " +
                "    d.%s as dict_code, " +
                "    d.%s as dict_name, " +
                "    i.%s as item_code, " +
                "    i.%s as item_desc " +
                "FROM " +
                "    %s d " +
                "LEFT JOIN " +
                "    %s i ON d.%s = i.%s " +
                "ORDER BY " +
                "    d.%s, i.%s",
                config.dictIdColumn, config.dictCodeColumn, config.dictNameColumn,
                config.dictItemCodeColumn, config.dictItemNameColumn,
                config.dictTableName, config.dictItemTableName,
                config.dictIdColumn, config.dictItemForeignKeyColumn,
                config.dictCodeColumn, config.dictItemCodeColumn
            );
            
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                
                Map<String, List<DictItem>> dictMap = new LinkedHashMap<>();
                Map<String, String> dictNameMap = new LinkedHashMap<>();
                
                while (resultSet.next()) {
                    String dictCode = resultSet.getString("dict_code");
                    String dictName = resultSet.getString("dict_name");
                    String itemCode = resultSet.getString("item_code");
                    String itemDesc = resultSet.getString("item_desc");
                    
                    dictNameMap.put(dictCode, dictName);
                    
                    if (itemCode != null && itemDesc != null) {
                        dictMap.computeIfAbsent(dictCode, k -> new ArrayList<>())
                               .add(new DictItem(itemCode, itemDesc));
                    }
                }
                
                if (dictMap.isEmpty()) {
                    messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "未找到任何字典数据,请检查数据库表 " + config.dictTableName + 
                        " 和 " + config.dictItemTableName + " 是否存在数据"
                    );
                    return Collections.emptyList();
                }
                
                messager.printMessage(Diagnostic.Kind.NOTE, "从数据库读取到 " + dictMap.size() + " 个字典");
                
                List<DictMetadata> result = new ArrayList<>();
                dictMap.forEach((dictCode, items) -> {
                    if (!items.isEmpty()) {
                        result.add(new DictMetadata(
                            dictCode,
                            dictNameMap.getOrDefault(dictCode, ""),
                            items
                        ));
                    }
                });
                
                return result;
            }
        }
    }
}
