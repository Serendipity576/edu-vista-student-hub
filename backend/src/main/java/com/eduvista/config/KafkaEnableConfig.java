package com.eduvista.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@EnableKafka
public class KafkaEnableConfig {
    // 这个配置类只在spring.kafka.bootstrap-servers存在时才会加载
    // 从而启用Kafka功能
}

