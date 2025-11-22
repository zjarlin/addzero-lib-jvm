package site.addzero.util.spring

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextHolder : ApplicationContextAware {
    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    companion object {
        private var applicationContext: ApplicationContext? = null

        fun getApplicationContext(): ApplicationContext {
            return applicationContext
                ?: throw IllegalStateException("ApplicationContext has not been initialized")
        }
    }
}
