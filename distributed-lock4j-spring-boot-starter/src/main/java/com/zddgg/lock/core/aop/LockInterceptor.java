package com.zddgg.lock.core.aop;

import com.zddgg.lock.core.LockFailureStrategy;
import com.zddgg.lock.core.LockInfo;
import com.zddgg.lock.core.LockKeyBuilder;
import com.zddgg.lock.core.LockTemplate;
import com.zddgg.lock.core.annotation.DistributedLock;
import com.zddgg.lock.core.autoconfigure.LockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class LockInterceptor implements MethodInterceptor {

    private final LockTemplate lockTemplate;

    private final LockKeyBuilder lockKeyBuilder;

    private final LockFailureStrategy lockFailureStrategy;

    private final LockProperties lockProperties;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //fix 使用其他aop组件时,aop切了两次.
        Class<?> cls = AopProxyUtils.ultimateTargetClass(Objects.requireNonNull(invocation.getThis()));
        if (!cls.equals(invocation.getThis().getClass())) {
            return invocation.proceed();
        }
        DistributedLock lock = invocation.getMethod().getAnnotation(DistributedLock.class);
        LockInfo lockInfo = null;
        try {
            String prefix = lockProperties.getLockKeyPrefix() + ":";
            prefix += StringUtils.hasText(lock.group()) ? lock.group() :
                    invocation.getMethod().getDeclaringClass().getName() + invocation.getMethod().getName();
            String lockKey = prefix + "#" + lockKeyBuilder.buildKey(invocation, lock.keys());
            lockInfo = lockTemplate.lock(lockKey, lock.expireTime(), lock.waitTime(), lock.executor());
            if (null != lockInfo) {
                return invocation.proceed();
            }
            // lock failure
            lockFailureStrategy.onLockFailure(lockKey, invocation.getMethod(), invocation.getArguments());
            return null;
        } finally {
            if (null != lockInfo) {
                final boolean releaseLock = lockTemplate.releaseLock(lockInfo);
                if (!releaseLock) {
                    log.error("releaseLock fail,lockKey={},lockValue={}", lockInfo.getLockKey(),
                            lockInfo.getLockValue());
                }
            }
        }
    }

}
