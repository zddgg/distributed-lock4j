package com.zddgg.lock.core;

import com.zddgg.lock.core.exception.LockFailureException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class DefaultLockFailureStrategy implements LockFailureStrategy {

    protected static String DEFAULT_MESSAGE = "request failed,please retry it.";

    @Override
    public void onLockFailure(String key, Method method, Object[] arguments) {
        throw new LockFailureException(DEFAULT_MESSAGE);
    }
}
