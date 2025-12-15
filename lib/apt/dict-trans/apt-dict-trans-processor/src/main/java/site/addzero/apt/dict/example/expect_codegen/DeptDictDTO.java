package site.addzero.apt.dict.example.expect_codegen;

import site.addzero.aop.dicttrans.anno.Dict;

public class DeptDictDTO {

    private Long id;


    private Integer status;

    // 省略 getter/setter
    private String status_dictText;


    private String otherTableFieldCode;
//由otherTableFieldCode 的将要生成的(伴生字段)
    private String myName;

    private String myName2;



    private String otherTableFieldCode1;
    //没有指定序列化别名,则xx_name => 转为 小驼峰命名  xxName作为期望生成的字段

    private String xxName;


}
