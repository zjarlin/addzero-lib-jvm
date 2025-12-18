package site.addzero.apt.dict.processor.test;

import lombok.Data;
import site.addzero.aop.dicttrans.anno.Dict;

@Data

public class TestRole {

    private Long id;

    private String roleName;

    // 角色类型（字典编码：ROLE_TYPE）
    @Dict("ROLE_TYPE")
    private String roleType;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
}