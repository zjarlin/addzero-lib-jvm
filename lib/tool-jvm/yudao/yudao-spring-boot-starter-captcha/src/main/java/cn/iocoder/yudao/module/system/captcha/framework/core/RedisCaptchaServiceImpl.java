package cn.iocoder.yudao.module.system.captcha.framework.core;

import com.anji.captcha.service.CaptchaCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisCaptchaServiceImpl implements CaptchaCacheService {

    private StringRedisTemplate stringRedisTemplate;

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        Boolean result = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Long increment(String key, long val) {
        return stringRedisTemplate.opsForValue().increment(key, val);
    }

}
