package site.addzero.apt;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典枚举代码生成器 - 生成 Java 风格的枚举类
 */
public class DictEnumCodeGenerator {

    private final Filer filer;
    private final Messager messager;
    private final String enumOutputPackage;

    public DictEnumCodeGenerator(Filer filer, Messager messager, String enumOutputPackage) {
        this.filer = filer;
        this.messager = messager;
        this.enumOutputPackage = enumOutputPackage;
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
            JavaFileObject fileObject = filer.createSourceFile(qualifiedName);
            try (PrintWriter writer = new PrintWriter(fileObject.openWriter())) {
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
                        String enumEntryName = toEnumEntryName(item.desc, item.code);
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
     */
    private String toEnumEntryName(String desc, String code) {
        // 优先使用 code，如果 code 为空则使用 desc
        String name = (code != null && !code.isEmpty()) ? code : desc;
        if (name == null || name.isEmpty()) {
            name = "UNKNOWN";
        }
        
        // 转换为大写并替换非法字符
        name = name.toUpperCase()
                   .replaceAll("[^A-Z0-9_]", "_")
                   .replaceAll("_+", "_");
        
        // 确保以字母或下划线开头
        if (!name.isEmpty() && Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        
        // 移除首尾下划线
        name = name.replaceAll("^_+|_+$", "");
        
        if (name.isEmpty()) {
            name = "UNKNOWN";
        }
        
        return name;
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
