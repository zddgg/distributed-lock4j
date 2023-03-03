package com.zddgg.lock.core.autoconfigure;

import com.zddgg.lock.core.executor.LockExecutor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "distributed.lock")
public class LockProperties {

    private String center = "redis";

    /**
     * 过期时间 单位：毫秒
     */
    private Long expireTime = 30000L;

    /**
     * 获取锁超时时间 单位：毫秒
     */
    private Long waitTime = 3000L;

    /**
     * 获取锁失败时重试时间间隔 单位：毫秒
     */
    private Long retryInterval = 100L;

    /**
     * 锁key前缀
     */
    private String lockKeyPrefix = "distributed-lock";

    private Class<? extends LockExecutor> primaryExecutor;
}
