package com.example.demo.finalprojectjavawebservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:";
    private static final String REVOKED_VALUE = "revoked";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String token, LocalDateTime expiresAt) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        if (ttl.isPositive()) {
            redisTemplate.opsForValue().set(buildKey(token), REVOKED_VALUE, ttl);
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(token)));
    }

    private String buildKey(String token) {
        return BLACKLIST_KEY_PREFIX + token;
    }
}
