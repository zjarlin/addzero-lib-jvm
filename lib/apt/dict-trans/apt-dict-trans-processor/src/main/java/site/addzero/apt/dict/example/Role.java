package site.addzero.apt.dict.example;

import site.addzero.aop.dicttrans.anno.Dict;

public class Role {

    private Long id;

    private String roleName;

    // 角色类型（字典编码：ROLE_TYPE）

    @Dict("ROLE_TYPE")

    private String roleType;

    // 省略 getter/setter

}