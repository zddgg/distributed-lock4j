package com.zddgg.lock.service;

import com.zddgg.lock.core.LockInfo;
import com.zddgg.lock.core.LockTemplate;
import com.zddgg.lock.core.annotation.DistributedLock;
import com.zddgg.lock.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    LockTemplate lockTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private int counter = 1;

    @Override
    @DistributedLock(keys = "#myKey")
    public void simple2(String myKey) {
        System.out.println("执行简单方法2 , 当前线程:" + Thread.currentThread().getName() + " , counter：" + (counter++));

    }

    @Override
    @DistributedLock(keys = "#user.id", expireTime = 1000, waitTime = 15000)
    public User method1(User user) {
        System.out.println("执行spel方法1 , 当前线程:" + Thread.currentThread().getName() + " , counter：" + (counter++));
        //模拟锁占用
        try {
            int count = 0;
            do {
                Thread.sleep(1000);
                System.out.println("执行spel方法1 , 当前线程:" + Thread.currentThread().getName() + " , 休眠秒：" + (count++));
            } while (count < 5);
//            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    @DistributedLock(keys = {"#user.id", "#user.name"}, expireTime = 5000, waitTime = 5000)
    public User method2(User user) {
        System.out.println("执行spel方法2 , 当前线程:" + Thread.currentThread().getName() + " , counter：" + (counter++));
        //模拟锁占用
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public void programmaticLock(String userId) {

        // 各种查询操作 不上锁
        // ...
        // 获取锁
        final LockInfo lockInfo = lockTemplate.lock(userId, 30000L, 5000L);
        if (null == lockInfo) {
            throw new RuntimeException("业务处理中,请稍后再试");
        }
        // 获取锁成功，处理业务
        try {
            System.out.println("执行简单方法1 , 当前线程:" + Thread.currentThread().getName() + " , counter：" + (counter++));
        } finally {
            //释放锁
            lockTemplate.releaseLock(lockInfo);
        }
        //结束
    }


    @Override
    @DistributedLock(keys = "1", expireTime = 60000)
    public void reentrantMethod1() {
        System.out.println("reentrantMethod1" + getClass());
        counter++;
    }

    @Override
    @DistributedLock(keys = "1")
    public void reentrantMethod2() {
        System.out.println("reentrantMethod2" + getClass());
        counter++;
    }

}