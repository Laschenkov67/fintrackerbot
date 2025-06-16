package com.example.fintrackerbot;

import com.example.fintrackerbot.services.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CacheServiceTest {

    @Autowired
    private CacheService redisCacheService;

    @Test
    public void testRedisSetGet() {
        String key = "testKey";
        String value = "testValue";

        redisCacheService.set(key, value, 60);
        Object result = redisCacheService.get(key);

        assertNotNull(result);
        assertEquals(value, result.toString());
    }

    @Test
    public void testRedisExistsAndDelete() {
        String key = "testKeyToDelete";
        redisCacheService.set(key, "value", 60);
        assertTrue(redisCacheService.exists(key));
        redisCacheService.delete(key);
        assertFalse(redisCacheService.exists(key));
    }
}
