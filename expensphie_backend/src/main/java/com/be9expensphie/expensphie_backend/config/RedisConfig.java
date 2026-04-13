package com.be9expensphie.expensphie_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){
        //config
        RedisCacheConfiguration redisCacheConfiguration= RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                )
                .disableCachingNullValues();
        //custom config for different service
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // settlement cache - 30 minutes TTL
        cacheConfigs.put("settlement", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)));
        // settlement stats cache - 10 minutes TTL
        cacheConfigs.put("settlement-stats-current-month", redisCacheConfiguration.entryTtl(Duration.ofMinutes(10)));
        // settlement stats cache - 30 minutes TTL
        cacheConfigs.put("settlement-stats-last-three-months", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)));
        // Products cache - 1 hour TTL
        cacheConfigs.put("products", redisCacheConfiguration.entryTtl(Duration.ofHours(1)));
        return RedisCacheManager.builder(connectionFactory)
                //if not customize so use this defaults config
                .cacheDefaults(redisCacheConfiguration)
                //intial will do this config
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
