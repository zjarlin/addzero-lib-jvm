package site.addzero.web.infra

import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import org.springframework.core.env.Environment
import java.util.Arrays

/**
 * @author addzero
 * @since 2022/10/8 11:11 AM
 */
object Envs {

    val currentEnv = if (isActive("local")) "local" else if (isActive("dev")) "dev" else if (isActive("test")) "test" else if (isActive("prod")) "prod" else ""

    fun isActive(envseq: String?): Boolean {
        val environment = SpringUtil.getBean(Environment::class.java)
        val activeProfiles: Array<String> = environment.activeProfiles
        return Arrays.stream(activeProfiles).anyMatch { env: String? ->
            StrUtil.containsIgnoreCase(envseq, env) || StrUtil.containsIgnoreCase(
                env, envseq
            )
        }
    }
}
