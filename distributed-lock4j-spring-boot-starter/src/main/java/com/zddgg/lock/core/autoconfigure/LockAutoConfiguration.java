package com.zddgg.lock.core.autoconfigure;

import com.zddgg.lock.core.*;
import com.zddgg.lock.core.aop.LockAnnotationAdvisor;
import com.zddgg.lock.core.aop.LockInterceptor;
import com.zddgg.lock.core.executor.LockExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration
@EnableConfigurationProperties(LockProperties.class)
@RequiredArgsConstructor
public class LockAutoConfiguration {

    private final LockProperties lockProperties;

    @SuppressWarnings("rawtypes")
    @Bean
    @ConditionalOnMissingBean
    public LockTemplate lockTemplate(List<LockExecutor> executors) {
        LockTemplate lockTemplate = new LockTemplate();
        lockTemplate.setProperties(lockProperties);
        lockTemplate.setExecutors(executors);
        return lockTemplate;
    }


    @Bean
    @ConditionalOnMissingBean
    public LockKeyBuilder lockKeyBuilder(BeanFactory beanFactory) {
        return new DefaultLockKeyBuilder(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockFailureStrategy lockFailureStrategy() {
        return new DefaultLockFailureStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public LockInterceptor lockInterceptor(@Lazy LockTemplate lockTemplate, LockKeyBuilder lockKeyBuilder,
                                           LockFailureStrategy lockFailureStrategy) {
        return new LockInterceptor(lockTemplate, lockKeyBuilder, lockFailureStrategy, lockProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockAnnotationAdvisor lockAnnotationAdvisor(LockInterceptor lockInterceptor) {
        return new LockAnnotationAdvisor(lockInterceptor, Ordered.HIGHEST_PRECEDENCE);
    }
}
