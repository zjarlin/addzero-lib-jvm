package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test

class DictTranslateProcessorTest {

    @Test
    fun testGeneratedCodeTemplate() {
        // Mock RBAC example: User entity with role field using table dictionary
        val userClassTemplate = """
            package com.example.rbac;

            import site.addzero.aop.dicttrans.anno.Dict;
            import java.util.List;

            public class User {
                private Long id;
                private String name;

                @Dict(tab = "role", codeColumn = "id", nameColumn = "name", whereCondition = "status = 1")
                private Long roleId;

                @Dict(dicCode = "user_status")
                private Integer status;

                // getters and setters
            }
        """.trimIndent()

        val generatedUserDictDslTemplate = """
            package com.example.rbac;

            import site.addzero.dict.trans.inter.TransApi;
            import site.addzero.dict.trans.inter.TableTranslateContext;
            import java.util.*;
            import java.util.concurrent.CompletableFuture;

            public class UserDictDsl extends User {
                private final TransApi transApi;

                public UserDictDsl(User original, TransApi transApi) {
                    // Copy all fields from original
                    this.setId(original.getId());
                    this.setName(original.getName());
                    this.setRoleId(original.getRoleId());
                    this.setStatus(original.getStatus());
                    this.transApi = transApi;
                }

                public static UserDictDsl translate(User user, TransApi transApi) {
                    UserDictDsl dsl = new UserDictDsl(user, transApi);
                    dsl.performTranslation();
                    return dsl;
                }

                public static List<UserDictDsl> translateList(List<User> users, TransApi transApi) {
                    List<UserDictDsl> result = new ArrayList<>();
                    for (User user : users) {
                        result.add(translate(user, transApi));
                    }
                    return result;
                }

                private void performTranslation() {
                    // Concurrent translation for system dict and table dict
                    CompletableFuture<Void> systemDictFuture = CompletableFuture.runAsync(() -> translateSystemDict());
                    CompletableFuture<Void> tableDictFuture = CompletableFuture.runAsync(() -> translateTableDict());

                    // Wait for both to complete
                    CompletableFuture.allOf(systemDictFuture, tableDictFuture).join();
                }

                private void translateSystemDict() {
                    // System dict translation: user_status
                    String dictCodes = "user_status";
                    String keys = String.valueOf(this.getStatus());
                    List<site.addzero.apt.dict.model.DictModel> dictModels = transApi.translateDictBatchCode2name(dictCodes, keys);
                    // Assuming single result, set translated value
                    if (!dictModels.isEmpty()) {
                        // Set translated status name, e.g., add a field or use existing
                        // For simplicity, assume we add a statusName field to UserDictDsl
                        this.statusName = dictModels.get(0).getLabel();
                    }
                }

                private void translateTableDict() {
                    // Table dict translation: role
                    List<TableTranslateContext> contexts = Arrays.asList(
                        new TableTranslateContext("role", "name", "id", String.valueOf(this.getRoleId()), "status = 1")
                    );

                    // Generate precompiled SQL at compile time
                    String precompiledSql = "SELECT id, name FROM role WHERE id IN (?) AND status = 1";
                    // At runtime, execute with actual values
                    List<Map<String, Object>> results = transApi.translateTableBatchCode2name("role", "name", "id", String.valueOf(this.getRoleId()));
                    if (!results.isEmpty()) {
                        this.roleName = (String) results.get(0).get("name");
                    }
                }

                // Additional fields for translated values
                private String statusName;
                private String roleName;

                // Getters for translated fields
                public String getStatusName() { return statusName; }
                public String getRoleName() { return roleName; }
            }
        """.trimIndent()

        println("User Class Template:")
        println(userClassTemplate)
        println("\nGenerated UserDictDsl Template:")
        println(generatedUserDictDslTemplate)
    }
}