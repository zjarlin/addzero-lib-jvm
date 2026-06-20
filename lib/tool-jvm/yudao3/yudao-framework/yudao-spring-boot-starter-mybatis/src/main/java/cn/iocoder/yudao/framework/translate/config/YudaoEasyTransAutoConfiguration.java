package cn.iocoder.yudao.framework.translate.config;

import com.fhs.cache.service.BothCacheService;
import com.fhs.cache.service.RedisCacheService;
import com.fhs.cache.service.TransCacheManager;
import com.fhs.common.constant.TransConfig;
import com.fhs.common.spring.SpringContextUtil;
import com.fhs.core.trans.convert.Convert;
import com.fhs.core.trans.util.ConvertUtil;
import com.fhs.trans.advice.EasyTransResponseBodyAdvice;
import com.fhs.trans.advice.ReleaseTransCacheAdvice;
import com.fhs.trans.aop.TransMethodResultAop;
import com.fhs.trans.controller.TransProxyController;
import com.fhs.trans.ds.DataSourceSetter;
import com.fhs.trans.listener.TransMessageListener;
import com.fhs.trans.service.impl.AutoTransService;
import com.fhs.trans.service.impl.DictionaryTransService;
import com.fhs.trans.service.impl.EnumTransService;
import com.fhs.trans.service.impl.RpcTransService;
import com.fhs.trans.service.impl.SimpleTransService;
import com.fhs.trans.service.impl.TransService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@Import(EasyTransResponseBodyAdvice.class)
public class YudaoEasyTransAutoConfiguration implements InitializingBean {

    @Value("${easy-trans.multiple-data-sources:false}")
    private boolean multipleDataSources;

    @Autowired(required = false)
    private DataSourceSetter dataSourceSetter;

    @Bean
    public TransService transService() {
        return new TransService();
    }

    @Bean
    @DependsOn("springContextUtil")
    public AutoTransService autoTransService() {
        return new AutoTransService();
    }

    @Bean
    public DictionaryTransService dictionaryTransService(SimpleTransService simpleTransService) {
        return new DictionaryTransService();
    }

    @Bean
    public ReleaseTransCacheAdvice releaseTransCacheAdvice() {
        return new ReleaseTransCacheAdvice();
    }

    @Bean
    public EnumTransService enumTransService() {
        return new EnumTransService();
    }

    @Bean
    @Primary
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public SimpleTransService simpleTransService(SimpleTransService.SimpleTransDiver driver) {
        SimpleTransService result = new SimpleTransService();
        result.regsiterTransDiver(driver);
        return result;
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public TransCacheManager transCacheManager() {
        return new TransCacheManager();
    }

    @Bean
    public BothCacheService bothCacheService() {
        return new BothCacheService();
    }

    @Bean
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public RpcTransService rpcTransService(SimpleTransService.SimpleTransDiver driver, RestTemplate restTemplate) {
        RpcTransService result = new RpcTransService();
        result.regsiterTransDiver(driver);
        result.setRestTemplate(restTemplate);
        return result;
    }

    @Bean
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public TransProxyController transProxyController(SimpleTransService.SimpleTransDiver driver) {
        TransProxyController result = new TransProxyController();
        result.setSimpleTransDiver(driver);
        return result;
    }

    @Bean
    public TransMethodResultAop transMethodResultAop() {
        return new TransMethodResultAop();
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    public TransMessageListener transMessageListener() {
        return new TransMessageListener();
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("trans"));
        container.setTopicSerializer(new StringRedisSerializer());
        return container;
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    MessageListenerAdapter listenerAdapter(TransMessageListener receiver, RedisTemplate<?, ?> redisTemplate) {
        MessageListenerAdapter result = new MessageListenerAdapter(receiver, "handelMsg");
        result.setSerializer(redisTemplate.getValueSerializer());
        return result;
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    public RedisCacheService redisCacheService(RedisTemplate<?, ?> redisTemplate, AutoTransService autoTransService) {
        RedisCacheService redisCacheService = new RedisCacheService();
        redisCacheService.setRedisTemplate(redisTemplate);
        redisCacheService.setStrRedisTemplate(redisTemplate);
        autoTransService.setRedisTransCache(redisCacheService);
        return redisCacheService;
    }

    @Bean("springContextUtil")
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Autowired(required = false)
    public void setConvertUtil(Convert convert) {
        ConvertUtil.setConvert(convert);
    }

    @Override
    public void afterPropertiesSet() {
        TransConfig.MULTIPLE_DATA_SOURCES = multipleDataSources;
        if (TransConfig.MULTIPLE_DATA_SOURCES && dataSourceSetter == null) {
            throw new IllegalArgumentException("easytrans 如果开启多数据源支持，需要自定义 DataSourceSetter 来切换数据源");
        }
        TransConfig.dataSourceSetter = dataSourceSetter;
    }
}
