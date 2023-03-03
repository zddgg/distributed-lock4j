package com.zddgg.lock.redis;

import com.zddgg.lock.core.autoconfigure.LockProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "distributed.lock.center", havingValue = "redis", matchIfMissing = true)
@ConditionalOnClass(RedisOperations.class)
@RequiredArgsConstructor
public class RedisTemplateLockAutoConfiguration {

    private final LockProperties lockProperties;

    @Bean
    @Order(200)
    public RedisTemplateLockExecutor redisTemplateLockExecutor(StringRedisTemplate stringRedisTemplate) {
        return new RedisTemplateLockExecutor(stringRedisTemplate, lockProperties);
    }
}