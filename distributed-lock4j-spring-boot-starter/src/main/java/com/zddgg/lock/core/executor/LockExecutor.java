package com.zddgg.lock.core.executor;

public interface LockExecutor<T> {

    default boolean renewal() {
        return false;
    }

    T lock(String lockKey, String lockValue, long leaseTime, long waitTime);

    boolean unlock(String key, String value, T lockInstance);

}
