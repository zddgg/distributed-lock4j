package com.zddgg.lock.service;

import com.zddgg.lock.model.User;

public interface UserService {

    void simple2(String myKey);

    User method1(User user);

    User method2(User user);

    void programmaticLock(String userId);

    void reentrantMethod1();

    void reentrantMethod2();

}
