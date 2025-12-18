package site.addzero.apt.dict.example;

import site.addzero.aop.dicttrans.anno.Dict;

import java.util.List;

public class User {

    private static final long serialVersionUID = 1L;
    
    public static final String USER_TYPE = "NORMAL";
    
    private static int userCount = 0;

    private Long id;

    private String userName;

    // user status (dict code: USER_STATUS), generates statusLabel field

    @Dict("USER_STATUS")
    private Integer status;

    // nested single entity: department (auto triggers Dept → DeptDictDTO conversion)

    private Dept dept;

    // nested collection: role list (auto triggers List<Role> → List<RoleDictDTO> conversion)

    private List<Role> roles;

    // omit getter/setter

}