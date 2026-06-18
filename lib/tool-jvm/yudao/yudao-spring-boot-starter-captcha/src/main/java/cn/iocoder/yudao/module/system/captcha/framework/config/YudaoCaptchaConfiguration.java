package cn.iocoder.yudao.module.system.captcha.framework.config;

import cn.iocoder.yudao.module.system.captcha.framework.core.RedisCaptchaServiceImpl;
import com.anji.captcha.config.AjCaptchaAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(AjCaptchaAutoConfiguration.class)
public class YudaoCaptchaConfiguration {

    @Bean(name = "AjCaptchaCacheService")
    @Primary
    public CaptchaCacheService captchaCacheService(AjCaptchaProperties config,
                                                   ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        CaptchaCacheService captchaCacheService = CaptchaServiceFactory.getCache(config.getCacheType().name());
        if (captchaCacheService instanceof RedisCaptchaServiceImpl) {
            StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
            if (stringRedisTemplate == null) {
                throw new IllegalStateException("StringRedisTemplate is required when aj.captcha.cache-type is redis");
            }
            ((RedisCaptchaServiceImpl) captchaCacheService).setStringRedisTemplate(stringRedisTemplate);
        }
        return captchaCacheService;
    }

}
