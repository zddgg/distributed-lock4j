package com.zddgg.lock.core;

import com.zddgg.lock.core.executor.LockExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockInfo {

    private String group;

    private String key;

    /**
     * 锁名称
     */
    private String lockKey;

    /**
     * 锁值
     */
    private String lockValue;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 获取锁超时时间
     */
    private Long waitTime;

    private Boolean lockResult;

    /**
     * 获取锁次数
     */
    private int tryLockCount;

    /**
     * 锁实例
     */
    private Object lockInstance;

    /**
     * 锁执行器
     */
    private LockExecutor lockExecutor;

    public LockInfo(String lockKey,
                    String lockValue,
                    long expireTime,
                    long waitTime,
                    int tryLockCount,
                    Object lockInstance,
                    LockExecutor lockExecutor) {
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.expireTime = expireTime;
        this.waitTime = waitTime;
        this.tryLockCount = tryLockCount;
        this.lockInstance = lockInstance;
        this.lockInstance = lockExecutor;
    }
}
