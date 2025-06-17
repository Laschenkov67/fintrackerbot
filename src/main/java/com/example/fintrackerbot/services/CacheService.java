package com.example.fintrackerbot.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getFromCache(String key) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    public void putToCache(String key, String value, long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public void evictFromCache(String key) {
        redisTemplate.delete(key);
    }
}
