//package com.addzero.lib_adpator
//
//import com.addzero.aop.dicttrans.anno.Dict
//import com.addzero.entity.GenderTest
//import com.addzero.entity.Res
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//class TestDictController {
//
//    @Dict
//    @GetMapping("/testDict")
//    fun dajsoidjiodsa(): Res<GenderTest> {
//        val apply = GenderTest("1", "130600")
//        return Res.success(apply)
//    }
//
//
//    @Dict("sys_gender")
//    @GetMapping("/testDict1")
//    fun dajsoidjiodsa1(): String {
//        return "1"
//    }
//
//    @Dict(tab = "sys_area", codeColumn = "area_code", nameColumn = "name")
//    @GetMapping("/testDict2")
//    fun dajsoidjidaosidjodsa1(): String {
//        return "130600"
//    }
//
//    @GetMapping("/testDict3")
//    fun dajsoidoaisjdjdjidaosidjodsa1(): GenderTest {
//        val apply = GenderTest("1", "130600")
//        return apply
//    }
//
//
//}
