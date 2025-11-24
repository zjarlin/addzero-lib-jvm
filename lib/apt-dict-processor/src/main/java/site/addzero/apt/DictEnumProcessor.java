package site.addzero.apt;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.sql.SQLException;
import java.util.*;

/**
 * 字典枚举 APT 处理器
 * 
 * 从数据库字典表生成 Java 风格的枚举类
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DictEnumProcessor extends AbstractProcessor {

    private DictMetadataExtractor metadataExtractor;
    private DictEnumCodeGenerator enumCodeGenerator;
    private List<DictMetadataExtractor.DictMetadata> dictMetadataList;
    private boolean processed = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        Map<String, String> options = processingEnv.getOptions();
        
        try {
            DictMetadataExtractor.DictConfig config = new DictMetadataExtractor.DictConfig(
                getOption(options, "jdbcDriver", "org.postgresql.Driver"),
                getRequiredOption(options, "jdbcUrl"),
                getRequiredOption(options, "jdbcUsername"),
                getRequiredOption(options, "jdbcPassword"),
                getOption(options, "dictTableName", "sys_dict"),
                getOption(options, "dictIdColumn", "id"),
                getOption(options, "dictCodeColumn", "dict_code"),
                getOption(options, "dictNameColumn", "dict_name"),
                getOption(options, "dictItemTableName", "sys_dict_item"),
                getOption(options, "dictItemForeignKeyColumn", "dict_id"),
                getOption(options, "dictItemCodeColumn", "item_value"),
                getOption(options, "dictItemNameColumn", "item_text")
            );
            
            String enumOutputPackage = getRequiredOption(options, "enumOutputPackage");
            
            this.metadataExtractor = new DictMetadataExtractor(processingEnv.getMessager(), config);
            this.enumCodeGenerator = new DictEnumCodeGenerator(
                processingEnv.getFiler(),
                processingEnv.getMessager(),
                enumOutputPackage
            );
        } catch (IllegalArgumentException e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "初始化失败: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed || roundEnv.processingOver()) {
            return false;
        }
        
        processed = true;
        
        try {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "开始收集字典元数据...");
            dictMetadataList = metadataExtractor.extractDictMetadata();
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "成功收集到 " + dictMetadataList.size() + " 个字典的元数据"
            );
            
            if (!dictMetadataList.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "开始生成 Java 枚举类...");
                enumCodeGenerator.generateEnumClasses(dictMetadataList);
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "成功生成 " + dictMetadataList.size() + " 个 Java 枚举类"
                );
            }
        } catch (ClassNotFoundException e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "⚠️ 找不到JDBC驱动: " + e.getMessage() + "\n跳过字典枚举生成过程"
            );
        } catch (SQLException e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                String.format("⚠️ SQL错误: %s, 错误代码: %d, SQL状态: %s\n跳过字典枚举生成过程",
                    e.getMessage(), e.getErrorCode(), e.getSQLState())
            );
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "⚠️ 无法连接到数据库或提取字典数据: " + e.getMessage() + "\n跳过字典枚举生成过程"
            );
        }
        
        return false;
    }

    private String getOption(Map<String, String> options, String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    private String getRequiredOption(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }
}
