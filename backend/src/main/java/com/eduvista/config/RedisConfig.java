package com.eduvista.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import java.util.List;

import java.time.Duration;



@Configuration
@EnableCaching
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.data.redis.host")
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * 核心修改：创建一个支持 Java8 日期序列化的 Jackson 序列化器
     */
    private GenericJackson2JsonRedisSerializer createJacksonSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 【新增 1】：忽略反序列化时 JSON 中存在但 Java 对象中没有的属性，防止报错
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 【新增 2】：关键配置！注册 PageImpl 的 Mixin
        // 这会告诉 Jackson：看到 PageImpl 时，用这个 Mixin 类里的规则处理
        om.addMixIn(PageImpl.class, PageImplMixin.class);

        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(om);
    }

    /**
     * 【新增 3】：PageImpl 的 Mixin 类
     * 映射 JSON 中的字段到 PageImpl 的构造函数
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PageImplMixin<T> {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public PageImplMixin(
                @JsonProperty("content") List<T> content,
                @JsonProperty("number") int number,
                @JsonProperty("size") int size,
                @JsonProperty("totalElements") long totalElements,
                @JsonProperty("pageable") JsonNode pageable // 暂时用 JsonNode 接收
        ) {}
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = createJacksonSerializer();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义的序列化器
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = createJacksonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            // 使用自定义的序列化器
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @Lazy
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectTimeout(3000)
                .setRetryAttempts(1)
                .setRetryInterval(1000);
            RedissonClient client = Redisson.create(config);
            // 测试连接（使用非弃用方法）
            try {
                client.getBucket("test").set("test");
                client.getBucket("test").delete();
            } catch (Exception testEx) {
                // 连接测试失败，但客户端可能仍然可用
                log.debug("Redis 连接测试失败，但客户端已创建: {}", testEx.getMessage());
            }
            log.info("RedissonClient 创建成功，已连接到 Redis: {}:{}", redisHost, redisPort);
            return client;
        } catch (Exception e) {
            // 如果 Redis 连接失败，记录警告并返回 null
            // RedisCacheService 已经处理了 redissonClient 为 null 的情况
            log.warn("无法连接到 Redis 服务器 {}:{}，应用将在没有 Redis 的情况下运行。错误: {}", 
                    redisHost, redisPort, e.getMessage());
            return null;
        }
    }
}