package site.addzero.apt.dict.example;

import site.addzero.aop.dicttrans.anno.Dict;

public class Dept {

    private Long id;

    // 部门状态（字典编码：DEPT_STATUS）

    @Dict("DEPT_STATUS")

    private Integer status;

    // 省略 getter/setter


//注意支持可重复注解
    @Dict(tab = "t_other",codeColumn = "xx_code",nameColumn = "xx_name",serializationAlias = "myName")
    @Dict(tab = "t_other",codeColumn = "xx_code",nameColumn = "xx_other_name",serializationAlias = "myName2")
    private String otherTableFieldCode;

    //没有指定序列化别名,则xx_name => 转为 小驼峰命名  xxName作为期望生成的字段
    @Dict(tab = "t_other",codeColumn = "xx_code",nameColumn = "xx_name")
    private String otherTableFieldCode1;

}