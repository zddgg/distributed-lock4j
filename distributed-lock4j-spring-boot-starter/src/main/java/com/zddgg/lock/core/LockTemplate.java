package com.zddgg.lock.core;

import com.zddgg.lock.core.autoconfigure.LockProperties;
import com.zddgg.lock.core.exception.LockException;
import com.zddgg.lock.core.executor.LockExecutor;
import com.zddgg.lock.core.utils.LockUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
@Slf4j
public class LockTemplate implements InitializingBean {

    private final Map<Class<? extends LockExecutor>, LockExecutor> executorMap = new LinkedHashMap<>();
    @Setter
    private LockProperties properties;
    @Setter
    private List<LockExecutor> executors;

    private LockExecutor primaryExecutor;

    public LockTemplate() {
    }

    public LockInfo lock(String key) {
        return lock(key, 0, -1);
    }

    public LockInfo lock(String key, long expire, long acquireTimeout) {
        return lock(key, expire, acquireTimeout, null);
    }

    /**
     * 加锁方法
     *
     * @param lockKey    锁key 同一个key只能被一个客户端持有
     * @param expireTime 过期时间(ms) 防止死锁
     * @param waitTime   尝试获取锁超时时间(ms)
     * @param executor   执行器
     * @return 加锁成功返回锁信息 失败返回null
     */
    public LockInfo lock(String lockKey, long expireTime, long waitTime, Class<? extends LockExecutor> executor) {
        waitTime = waitTime < 0 ? properties.getWaitTime() : waitTime;
        long retryInterval = properties.getRetryInterval();
        LockExecutor lockExecutor = obtainExecutor(executor);
        log.debug(String.format("use lock class: %s", lockExecutor.getClass()));
        expireTime = !lockExecutor.renewal() && expireTime <= 0 ? properties.getExpireTime() : expireTime;
        int acquireCount = 0;
        String lockValue = LockUtil.simpleUUID();
        long start = System.currentTimeMillis();
        try {
            do {
                acquireCount++;
                Object lockInstance = lockExecutor.lock(lockKey, lockValue, expireTime, waitTime);
                if (null != lockInstance) {
                    return new LockInfo(lockKey, lockValue, expireTime, waitTime, acquireCount, lockInstance,
                            lockExecutor);
                }
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } while (System.currentTimeMillis() - start < waitTime);
        } catch (InterruptedException e) {
            log.error("lock error", e);
            throw new LockException();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean releaseLock(LockInfo lockInfo) {
        if (null == lockInfo) {
            return false;
        }
        return lockInfo.getLockExecutor().unlock(lockInfo.getLockKey(), lockInfo.getLockValue(),
                lockInfo.getLockInstance());
    }

    protected LockExecutor obtainExecutor(Class<? extends LockExecutor> clazz) {
        if (null == clazz || clazz == LockExecutor.class) {
            return primaryExecutor;
        }
        final LockExecutor lockExecutor = executorMap.get(clazz);
        Assert.notNull(lockExecutor, String.format("can not get bean type of %s", clazz));
        return lockExecutor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.isTrue(properties.getWaitTime() >= 0, "waitTime must least 0");
        Assert.isTrue(properties.getExpireTime() >= -1, "expireTime must lease -1");
        Assert.isTrue(properties.getRetryInterval() >= 0, "retryInterval must more than 0");
        Assert.hasText(properties.getLockKeyPrefix(), "lock key prefix must be not blank");
        Assert.notEmpty(executors, "executors must have at least one");

        for (LockExecutor executor : executors) {
            executorMap.put(executor.getClass(), executor);
        }

        final Class<? extends LockExecutor> primaryExecutor = properties.getPrimaryExecutor();
        if (null == primaryExecutor) {
            this.primaryExecutor = executors.get(0);
        } else {
            this.primaryExecutor = executorMap.get(primaryExecutor);
            Assert.notNull(this.primaryExecutor, "primaryExecutor must be not null");
        }
    }
}
