package site.addzero.apt;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典枚举代码生成器 - 生成 Java 风格的枚举类
 */
public class DictEnumCodeGenerator {

    private final Filer filer;
    private final Messager messager;
    private final String enumOutputPackage;
    private final String customOutputDirectory;  // 自定义输出目录
    private final boolean useCustomOutput;       // 是否使用自定义输出

    public DictEnumCodeGenerator(Filer filer, Messager messager, String enumOutputPackage) {
        this(filer, messager, enumOutputPackage, null);
    }

    public DictEnumCodeGenerator(Filer filer, Messager messager, String enumOutputPackage, String customOutputDirectory) {
        this.filer = filer;
        this.messager = messager;
        this.enumOutputPackage = enumOutputPackage;
        this.customOutputDirectory = customOutputDirectory;
        this.useCustomOutput = customOutputDirectory != null && !customOutputDirectory.trim().isEmpty();
        
        if (useCustomOutput) {
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "使用自定义输出目录: " + customOutputDirectory);
        }
    }

    /**
     * 生成所有字典的枚举类
     */
    public void generateEnumClasses(List<DictMetadataExtractor.DictMetadata> dictMetadataList) {
        Set<String> generatedClassNames = new HashSet<>();

        dictMetadataList.forEach(dictMetadata -> {
            try {
                if (dictMetadata.items.isEmpty()) {
                    messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "字典'" + dictMetadata.dictCode + "'没有字典项,跳过枚举生成"
                    );
                    return;
                }

                String enumName = toCamelCase(dictMetadata.dictCode, true);

                if (generatedClassNames.contains(enumName)) {
                    messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "字典'" + dictMetadata.dictCode + "'生成的枚举类名'" + enumName + "'重复,跳过"
                    );
                    return;
                }

                generatedClassNames.add(enumName);
                generateEnumClass(dictMetadata, enumName);
            } catch (Exception e) {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "为字典'" + dictMetadata.dictCode + "'生成枚举类失败: " + e.getMessage()
                );
            }
        });
    }

    /**
     * 生成单个枚举类
     */
    private void generateEnumClass(DictMetadataExtractor.DictMetadata dictMetadata, String enumName) {
        String fullEnumName = "Enum" + enumName;
        String qualifiedName = enumOutputPackage + "." + fullEnumName;

        try {
            PrintWriter writer = null;
            
            try {
                if (useCustomOutput) {
                    // 使用自定义输出目录
                    writer = createCustomOutputWriter(fullEnumName);
                } else {
                    // 使用标准 Filer API（生成到 target/generated-sources/annotations）
                    JavaFileObject fileObject = filer.createSourceFile(qualifiedName);
                    writer = new PrintWriter(fileObject.openWriter());
                }
                writer.println("package " + enumOutputPackage + ";");
                writer.println();
                writer.println("/**");
                writer.println(" * " + dictMetadata.dictName);
                writer.println(" *");
                writer.println(" * 数据库字典编码: " + dictMetadata.dictCode);
                writer.println(" * 自动生成的枚举类，不要手动修改");
                writer.println(" */");
                writer.println("public enum " + fullEnumName + " {");
                writer.println();
                
                // 生成枚举常量
                List<String> enumConstants = dictMetadata.items.stream()
                    .map(item -> {
                        // 使用 code 作为枚举值名称，确保处理数字开头的情况
                        String enumEntryName = toEnumEntryName(item.code);
                        return String.format("    %s(\"%s\", \"%s\")",
                            enumEntryName,
                            escapeJavaString(item.code),
                            escapeJavaString(item.desc));
                    })
                    .collect(Collectors.toList());
                
                writer.println(String.join(",\n", enumConstants) + ";");
                writer.println();
                
                // 生成字段
                writer.println("    private final String code;");
                writer.println("    private final String desc;");
                writer.println();
                
                // 生成构造函数
                writer.println("    " + fullEnumName + "(String code, String desc) {");
                writer.println("        this.code = code;");
                writer.println("        this.desc = desc;");
                writer.println("    }");
                writer.println();
                
                // 生成 getter 方法
                writer.println("    public String getCode() {");
                writer.println("        return code;");
                writer.println("    }");
                writer.println();
                writer.println("    public String getDesc() {");
                writer.println("        return desc;");
                writer.println("    }");
                writer.println();
                
                // 生成工具方法
                writer.println("    /**");
                writer.println("     * 根据编码获取枚举值");
                writer.println("     *");
                writer.println("     * @param code 编码");
                writer.println("     * @return 对应的枚举值，如果不存在则返回null");
                writer.println("     */");
                writer.println("    public static " + fullEnumName + " fromCode(String code) {");
                writer.println("        if (code == null) {");
                writer.println("            return null;");
                writer.println("        }");
                writer.println("        for (" + fullEnumName + " e : values()) {");
                writer.println("            if (e.code.equals(code)) {");
                writer.println("                return e;");
                writer.println("            }");
                writer.println("        }");
                writer.println("        return null;");
                writer.println("    }");
                writer.println();
                writer.println("    /**");
                writer.println("     * 根据描述获取枚举值");
                writer.println("     *");
                writer.println("     * @param desc 描述");
                writer.println("     * @return 对应的枚举值，如果不存在则返回null");
                writer.println("     */");
                writer.println("    public static " + fullEnumName + " fromDesc(String desc) {");
                writer.println("        if (desc == null) {");
                writer.println("            return null;");
                writer.println("        }");
                writer.println("        for (" + fullEnumName + " e : values()) {");
                writer.println("            if (e.desc.equals(desc)) {");
                writer.println("                return e;");
                writer.println("            }");
                writer.println("        }");
                writer.println("        return null;");
                writer.println("    }");
                
                writer.println("}");
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, "Generated enum class: " + fullEnumName);
        } catch (IOException e) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to create enum class " + fullEnumName + ": " + e.getMessage()
            );
        }
    }

    /**
     * 转换为驼峰命名
     */
    private String toCamelCase(String str, boolean capitalizeFirst) {
        String[] words = str.replaceAll("[_\\-\\s]+", " ")
                            .split(" ");
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) continue;
            
            if (i == 0 && !capitalizeFirst) {
                result.append(word.toLowerCase());
            } else {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 转换为合法的枚举项名称
     * 规则：
     * 1. 如果是纯数字，直接加下划线前缀: "0" -> "_0", "123" -> "_123"
     * 2. 如果以数字开头，加下划线前缀: "10min" -> "_10MIN", "1abc" -> "_1ABC"
     * 3. 其他情况转大写并替换非法字符: "status" -> "STATUS"
     */
    private String toEnumEntryName(String code) {
        if (code == null || code.isEmpty()) {
            return "UNKNOWN";
        }
        
        // 如果整个code都是数字，直接加下划线前缀并返回
        if (code.matches("^\\d+$")) {
            return "_" + code;
        }
        
        // 转换为大写并替换非法字符（只保留字母、数字、下划线）
        String processed = code.toUpperCase()
                   .replaceAll("[^A-Z0-9_]", "_")
                   .replaceAll("_+", "_");
        
        // 移除尾部下划线
        processed = processed.replaceAll("_+$", "");
        
        // 如果以数字开头，加下划线前缀
        if (!processed.isEmpty() && Character.isDigit(processed.charAt(0))) {
            processed = "_" + processed;
        }
        
        // 移除开头的多余下划线（但保留用于数字开头的那一个）
        // 例如：___ABC -> _ABC（如果ABC是数字开头则保留一个下划线）
        // 注意：这里不要移除用于数字开头的下划线
        if (processed.startsWith("__")) {
            // 如果有多个下划线，只保留一个
            processed = processed.replaceAll("^_+", "_");
        }
        
        // 如果处理后为空或只有下划线，使用默认值
        if (processed.isEmpty() || processed.matches("^_+$")) {
            return "UNKNOWN";
        }
        
        return processed;
    }

    /**
     * 创建自定义输出目录的 Writer
     */
    private PrintWriter createCustomOutputWriter(String fullEnumName) throws IOException {
        // 将包名转换为目录路径
        String packagePath = enumOutputPackage.replace('.', File.separatorChar);
        
        // 构建完整路径
        Path outputPath = Paths.get(customOutputDirectory, packagePath);
        
        // 创建目录（如果不存在）
        Files.createDirectories(outputPath);
        
        // 创建文件
        Path filePath = outputPath.resolve(fullEnumName + ".java");
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "生成枚举到自定义目录: " + filePath);
        
        return new PrintWriter(Files.newBufferedWriter(filePath));
    }

    /**
     * 转义 Java 字符串
     */
    private String escapeJavaString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
