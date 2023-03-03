package com.zddgg.lock.core;

import org.aopalliance.intercept.MethodInvocation;

public interface LockKeyBuilder {

    String buildKey(MethodInvocation invocation, String[] definitionKeys);
}
