package site.addzero.entity

import site.addzero.aop.dicttrans.anno.Dict

open class GenderTest(
    @Dict("sys_gender")
    var gender: String = "",

    @Dict(tab = "sys_area", codeColumn = "area_code", nameColumn = "name")
    @Dict(tab = "sys_area", codeColumn = "area_code", nameColumn = "node_type")
    var areaCode: String = ""
)
