package com.be9expensphie.expensphie_backend.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){
        //object mapper for java->json
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        //stamp "20-2-222", overwrite default config
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        //tell this serializer to use objectMapper
        //actual engine that serialize+desirialize
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        //config
        RedisCacheConfiguration redisCacheConfiguration= RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                serializer
                        )
                )
                .disableCachingNullValues();
        //custom config for different service
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // AI suggestion cache - 1h TTL
        cacheConfigs.put("ai_suggestion",redisCacheConfiguration.entryTtl(Duration.ofHours(1)));
        // settlement stats cache - 10 minutes TTL
        cacheConfigs.put("settlement-stats-current-month", redisCacheConfiguration.entryTtl(Duration.ofMinutes(10)));
        // settlement stats cache - 30 minutes TTL
        cacheConfigs.put("settlement-stats-last-three-months", redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)));
        // Products cache - 1 hour TTL
        cacheConfigs.put("products", redisCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("expense_in_range",redisCacheConfiguration.entryTtl(Duration.ofHours(1)));
        return RedisCacheManager.builder(connectionFactory)
                //if not customize so use this defaults config
                .cacheDefaults(redisCacheConfiguration)
                //intial will do this config
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }


    @Bean
    //this for manually set up k-v, directly interact with redis (when want to not only cache method)
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String,String> template=new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //redis stored in bytes-> need serializer when retrieve+ store
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        //define how to serialize
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
