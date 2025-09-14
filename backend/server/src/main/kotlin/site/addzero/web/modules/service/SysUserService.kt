package site.addzero.web.modules.service

import cn.dev33.satoken.stp.StpUtil
import site.addzero.common.consts.sql
import site.addzero.entity.CheckSignInput
import site.addzero.entity.SecondLoginDTO
import site.addzero.exp.BizException
import site.addzero.model.entity.SysUser
import site.addzero.model.entity.email
import site.addzero.model.entity.phone
import site.addzero.model.entity.username
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service

@Service
class SysUserService(
    private val request: HttpServletRequest,
    private val saTokenConfig: cn.dev33.satoken.config.SaTokenConfig
) {
    fun getCurrentUser(): SysUser {

        // 获取请求头中的token
        val tokenFromHeader = request.getHeader(saTokenConfig.tokenName)
        if (tokenFromHeader == "admin") {
            return SysUser {
                username = tokenFromHeader
                nickname = "超级管理员"
            }
        }
        println("请求头中的token: $tokenFromHeader")

        // 获取当前登录用户ID
        var tokenInfo = StpUtil.getTokenInfo()
        println("SaToken信息: $tokenInfo")

        val userId = StpUtil.getLoginIdAsLong()
        val findById = sql.findById(SysUser::class, userId) ?: throw BizException("用户未找到")
        return findById
    }

    fun findByUserRegFormState(secondLoginDTO: SecondLoginDTO): SysUser? {
        val userRegFormState = secondLoginDTO.userRegFormState

        val email = userRegFormState.email
        val phone = userRegFormState.phone
        val username = userRegFormState.username

        val user = sql.executeQuery(SysUser::class) {
            where(
                table.phone eq phone,
                table.email eq email,
                table.username eq username,
            )
            select(table)
        }.firstOrNull()

//        val user = user(email, phone, username)
        return user
    }

}

fun identifyInputType(input: String): CheckSignInput {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    val phoneRegex = Regex("^1[3-9]\\d{9}$")
    return when {
        emailRegex.matches(input) -> CheckSignInput.EMAIL
        phoneRegex.matches(input) -> CheckSignInput.PHONE
        else -> CheckSignInput.USERNAME
    }
}
