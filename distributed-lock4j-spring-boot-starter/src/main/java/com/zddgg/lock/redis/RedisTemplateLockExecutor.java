package com.zddgg.lock.redis;

import com.zddgg.lock.core.autoconfigure.LockProperties;
import com.zddgg.lock.core.exception.LockException;
import com.zddgg.lock.core.executor.AbstractLockExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class RedisTemplateLockExecutor extends AbstractLockExecutor<String> {

    private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>("return redis.call('set',KEYS[1]," +
            "ARGV[1],'NX','PX',ARGV[2])", String.class);
    private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>("if redis.call('get',KEYS[1]) " +
            "== ARGV[1] then return tostring(redis.call('del', KEYS[1])==1) else return 'false' end", String.class);
    private static final RedisScript<Boolean> SCRIPT_RENEWAL = new DefaultRedisScript<>("if redis.call('get', KEYS[1]) ==ARGV[1] " +
            "then return redis.call('pexpire', KEYS[1], ARGV[2]) else  return 0  end", Boolean.class);
    private static final String LOCK_SUCCESS = "OK";

    private final StringRedisTemplate redisTemplate;

    private final LockProperties lockProperties;

    @Override
    public boolean renewal() {
        return true;
    }

    @Override
    public String lock(String lockKey, String lockValue, long expire, long acquireTimeout) {

        final long newExpire = expire > 0 ? expire : lockProperties.getExpireTime();

        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> redisTemplate.execute(SCRIPT_LOCK,
                        redisTemplate.getStringSerializer(),
                        redisTemplate.getStringSerializer(),
                        Collections.singletonList(lockKey),
                        lockValue, String.valueOf(newExpire)))
                .thenApply(acquired -> {
                    //成功且传-1时开始续期
                    if (LOCK_SUCCESS.equals(acquired) && expire == -1) {
                        renewExpiration(newExpire, lockKey, lockValue);
                    }
                    return acquired;
                });
        String lock;
        try {
            lock = cf.get();
        } catch (Exception e) {
            log.error("lock error", e);
            throw new LockException();
        }
        final boolean locked = LOCK_SUCCESS.equals(lock);
        return obtainLockInstance(locked, lock);
    }

    @Override
    public boolean unlock(String key, String value, String lockInstance) {
        String releaseResult = redisTemplate.execute(SCRIPT_UNLOCK,
                redisTemplate.getStringSerializer(),
                redisTemplate.getStringSerializer(),
                Collections.singletonList(key), value);
        return Boolean.parseBoolean(releaseResult);
    }


    private void renewExpiration(long expire, String lockKey, String lockValue) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Boolean.TRUE.equals(redisTemplate.execute(SCRIPT_RENEWAL, Collections.singletonList(lockKey), lockValue, String.valueOf(expire)))) {
                    renewExpiration(expire, lockKey, lockValue);
                }
            }
        }, expire / 3);
    }


}
