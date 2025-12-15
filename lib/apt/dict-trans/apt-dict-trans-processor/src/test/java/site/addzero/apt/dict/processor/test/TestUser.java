package site.addzero.apt.dict.processor.test;

import site.addzero.aop.dicttrans.anno.Dict;

import java.util.List;

public class TestUser {

    private Long id;

    private String userName;

    // 用户状态（字典编码：USER_STATUS），生成 label 字段 statusLabel
    @Dict("USER_STATUS")
    private Integer status;

    // 嵌套单个实体：部门（自动触发 Dept → DeptDictDTO 转换）
    private TestDept dept;

    // 嵌套集合：角色列表（自动触发 List<Role> → List<RoleDictDTO> 转换）
    private List<TestRole> roles;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public TestDept getDept() {
        return dept;
    }

    public void setDept(TestDept dept) {
        this.dept = dept;
    }

    public List<TestRole> getRoles() {
        return roles;
    }

    public void setRoles(List<TestRole> roles) {
        this.roles = roles;
    }
}