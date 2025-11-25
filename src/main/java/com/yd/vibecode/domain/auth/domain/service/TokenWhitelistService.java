package com.yd.vibecode.domain.auth.domain.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenWhitelistService {
    private final RedisTemplate<String, String> redisTemplate;

    private final static String whitelistPrefix = "WHITELIST:";

    public boolean isWhitelistToken(String token) {
        String saved = redisTemplate.opsForValue().get(whitelistPrefix + token);
        boolean result = saved != null && saved.equals(token);
        return result;
    }


    public void whitelist(String token, Duration timeout) {
        redisTemplate.opsForValue().set(whitelistPrefix + token, token, timeout);
    }

    public void deleteWhitelistToken(String token) {
        redisTemplate.delete(whitelistPrefix + token);
    }
}