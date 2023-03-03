package com.zddgg.lock.core;

import java.lang.reflect.Method;

public interface LockFailureStrategy {

    void onLockFailure(String key, Method method, Object[] arguments);

}
