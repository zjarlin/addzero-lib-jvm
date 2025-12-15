package site.addzero.apt.dict.example.expect_codegen;

import java.util.List;

public class UserDictDTO {

    private Long id;

    private String userName;

    private Integer status;

    // 字典标签字段

    private String status_dictText;

    // 嵌套 Dept 的 DictDTO  (伴生)

    private DeptDictDTO dept;

    // 嵌套 Role 的 DictDTO 集合 (伴生)

    private List<RoleDictDTO> roles;

    // 省略 getter/setter

}