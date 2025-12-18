package com.eduvista.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private GenericJackson2JsonRedisSerializer createJacksonSerializer() {
        ObjectMapper om = new ObjectMapper();

        // 1. 基础配置
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 2. 注册模块 (Java8 时间 + Spring Data 分页支持)
        om.registerModule(new JavaTimeModule());

        // 3. 核心：使用 Mixin 处理 PageImpl
        // 注意：这里不再手动写复杂的构造函数匹配，而是利用 PageImpl 的特性
        om.addMixIn(PageImpl.class, PageImplMixin.class);
        om.addMixIn(Pageable.class, PageableMixin.class);

        // 4. 开启类型信息 (保持反序列化后的类型正确)
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(om);
    }

    /**
     * 更加稳健的 PageImpl Mixin
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PageImplMixin<T> {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public PageImplMixin(
                @JsonProperty("content") List<T> content,
                @JsonProperty("number") int number,
                @JsonProperty("size") int size,
                @JsonProperty("totalElements") long totalElements,
                @JsonProperty("pageable") Pageable pageable // 使用 Pageable 接口
        ) {}
    }

    /**
     * 必须同时提供 Pageable 的 Mixin，因为 PageImpl 构造函数依赖它
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PageableMixin {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public static PageRequest of(@JsonProperty("pageNumber") int page, @JsonProperty("pageSize") int size) {
            return PageRequest.of(page, size);
        }
    }

    // --- 剩下的 Bean 定义保持不变 ---

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = createJacksonSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
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
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}