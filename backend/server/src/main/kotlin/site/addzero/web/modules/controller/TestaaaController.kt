package site.addzero.web.modules.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestaaaController {
    @GetMapping("dajsiod")
    fun djasoid(): Int {
        return 1/0

    }
}
