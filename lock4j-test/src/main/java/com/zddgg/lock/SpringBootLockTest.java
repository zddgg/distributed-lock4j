package com.zddgg.lock;

import com.zddgg.lock.model.User;
import com.zddgg.lock.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
@SpringBootTest(classes = SpringBootLockTest.class)
@SpringBootApplication
public class SpringBootLockTest {

    private static final Random RANDOM = new Random();

    @Autowired
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLockTest.class, args);
    }

    @SneakyThrows
    @Test
    public void spel1Test() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    userService.method1(new User(1L, "苞米豆"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    @SneakyThrows
    @Test
    public void spel2Test() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    userService.method2(new User(1L, "苞米豆"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * 编程式锁
     */
    @SneakyThrows
    @Test
    public void programmaticLock() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                userService.programmaticLock("admin");
            }
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * 重入锁
     */
    @Test
    public void reentrantLock() {
        userService.reentrantMethod1();
        userService.reentrantMethod1();
        userService.reentrantMethod2();
    }

}