package com.yd.vibecode.global.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureRandomGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        int random = secureRandom.nextInt(1_000_000);
        return String.format("%06d", random);
    }

}