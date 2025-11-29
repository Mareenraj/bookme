package com.mareen.bookme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use Jackson2JsonRedisSerializer or JdkSerializationRedisSerializer for values
        // Option 1: String serializer (recommended for simple string values like OTP)
        template.setValueSerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());

        // Option 2: If you need JSON serialization, use Jackson2JsonRedisSerializer
        // template.setValueSerializer(RedisSerializer.json());
        // template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}