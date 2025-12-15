package site.addzero.apt.dict.processor.test;

import site.addzero.aop.dicttrans.anno.Dict;

public class TestDept {

    private Long id;

    // 部门状态（字典编码：DEPT_STATUS）
    @Dict("DEPT_STATUS")
    private Integer status;

    // 注意支持可重复注解
    @Dict(tab = "t_other", codeColumn = "xx_code", nameColumn = "xx_name", serializationAlias = "myName")
    @Dict(tab = "t_other", codeColumn = "xx_code", nameColumn = "xx_other_name", serializationAlias = "myName2")
    private String otherTableFieldCode;

    // 没有指定序列化别名,则xx_name => 转为 小驼峰命名  xxName作为期望生成的字段
    @Dict(tab = "t_other", codeColumn = "xx_code", nameColumn = "xx_name")
    private String otherTableFieldCode1;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOtherTableFieldCode() {
        return otherTableFieldCode;
    }

    public void setOtherTableFieldCode(String otherTableFieldCode) {
        this.otherTableFieldCode = otherTableFieldCode;
    }

    public String getOtherTableFieldCode1() {
        return otherTableFieldCode1;
    }

    public void setOtherTableFieldCode1(String otherTableFieldCode1) {
        this.otherTableFieldCode1 = otherTableFieldCode1;
    }
}