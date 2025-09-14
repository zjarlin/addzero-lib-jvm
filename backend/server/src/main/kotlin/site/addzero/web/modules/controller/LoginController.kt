package site.addzero.web.modules.controller

import cn.dev33.satoken.stp.StpUtil
import site.addzero.common.consts.sql
import site.addzero.entity.SecondLoginDTO
import site.addzero.entity.SecondLoginResponse
import site.addzero.entity.SignInStatus
import site.addzero.exp.BizException
import site.addzero.generated.isomorphic.SysUserIso
import site.addzero.model.entity.SysUser
import site.addzero.model.entity.email
import site.addzero.model.entity.phone
import site.addzero.model.entity.username
import site.addzero.web.infra.jackson.convertTo
import site.addzero.web.infra.jimmer.toJimmerEntity
import site.addzero.web.modules.service.SysUserService
import site.addzero.web.modules.service.identifyInputType
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.springframework.web.bind.annotation.*

@RequestMapping("/sys/login")
@RestController
class LoginController(private val sysUserService: SysUserService) {

    @GetMapping("/hasPermition")
    fun hasPermition(@RequestParam code: String): Boolean {
        return true
    }

    @PostMapping("/signin")
    fun signin(@RequestBody loginRe: String): SignInStatus {

        val executeQuery = sql.executeQuery(SysUser::class) {
            where(
                or(
                    table.username eq loginRe,
                    table.email eq loginRe,
                    table.phone eq loginRe,
                )
            )
            select(table)
        }.firstOrNull()
        val checkSignInput = identifyInputType(loginRe)

        if (executeQuery == null) {
            val notregister = SignInStatus.Notregister(checkSignInput)
            return notregister
        }

        val sysUserIso = executeQuery.convertTo<SysUserIso>()
        val alredyregister = SignInStatus.Alredyregister(checkSignInput, sysUserIso)
        return alredyregister
    }
//    fun

    /**
     * 注册一个用户
     * @param [userRegFormState]
     * @return [Boolean]
     */
    @PostMapping("/signup")
    fun signup(@RequestBody userRegFormState: SysUserIso): Boolean {
        val toJimmerEntity = userRegFormState.toJimmerEntity<SysUserIso, SysUser>()

        val save = sql.save(toJimmerEntity)
        val rowAffected = save.isRowAffected
        val modified = save.isModified

        return rowAffected
//        log.info(save.modifiedEntity)
    }

    @PostMapping("/signinSecond")
    fun signinSecond(@RequestBody secondLoginDTO: SecondLoginDTO): SecondLoginResponse {
        val findByUserRegFormState =
            sysUserService.findByUserRegFormState(secondLoginDTO) ?: throw BizException("用户不存在")
        val password = secondLoginDTO.userRegFormState.password
        if (findByUserRegFormState.password != password) {
            throw BizException("密码错误")
        }
        StpUtil.login(findByUserRegFormState.id)
        val tokenInfo = StpUtil.getTokenInfo()
        val tokenValue = tokenInfo.tokenValue
        val secondLoginResponse = SecondLoginResponse(findByUserRegFormState.convertTo(), tokenValue)
        return secondLoginResponse

    }

}
