package com.zddgg.lock.core.annotation;

import com.zddgg.lock.core.executor.LockExecutor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface DistributedLock {

    String group() default "";

    String[] keys();

    long expireTime() default -1;

    long waitTime() default -1;

    Class<? extends LockExecutor> executor() default LockExecutor.class;

}
