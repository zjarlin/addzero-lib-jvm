package site.addzero.aop.dicttrans.util

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Spring工具类，替代hutool的SpringUtil
 * 
 * @author zjarlin
 */
@Component
class SpringUtil : ApplicationContextAware {
    
    companion object {
        private var applicationContext: ApplicationContext? = null
        
        /**
         * 获取Bean
         */
        fun <T> getBean(clazz: Class<T>): T {
            return applicationContext?.getBean(clazz) 
                ?: throw IllegalStateException("ApplicationContext not initialized")
        }
        
        /**
         * 根据名称获取Bean
         */
        fun getBean(name: String): Any {
            return applicationContext?.getBean(name) 
                ?: throw IllegalStateException("ApplicationContext not initialized")
        }
        
        /**
         * 根据名称和类型获取Bean
         */
        fun <T> getBean(name: String, clazz: Class<T>): T {
            return applicationContext?.getBean(name, clazz) 
                ?: throw IllegalStateException("ApplicationContext not initialized")
        }
        
        /**
         * 获取ApplicationContext
         */
        fun getApplicationContext(): ApplicationContext? {
            return applicationContext
        }
    }
    
    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringUtil.applicationContext = applicationContext
    }
}