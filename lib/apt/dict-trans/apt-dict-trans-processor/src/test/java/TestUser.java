package site.addzero.apt.dict.processor.test;

import site.addzero.aop.dicttrans.anno.Dict;

public class TestUser {
    private Long id;
    private String name;

    @Dict(tab = "role", codeColumn = "id", nameColumn = "name", whereCondition = "status = 1")
    private Long roleId;

    @Dict(dicCode = "user_status")
    private Integer status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}