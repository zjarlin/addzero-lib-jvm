package site.addzero.apt.dict.example;

import site.addzero.aop.dicttrans.anno.Dict;

import java.util.List;

public class User {

    private Long id;

    private String userName;

    // 用户状态（字典编码：USER_STATUS），生成 label 字段 statusLabel

    @Dict( "USER_STATUS")
    private Integer status;

    // 嵌套单个实体：部门（自动触发 Dept → DeptDictDTO 转换）

    private Dept dept;

    // 嵌套集合：角色列表（自动触发 List<Role> → List<RoleDictDTO> 转换）

    private List<Role> roles;

    // 省略 getter/setter

}