package com.yd.vibecode.global.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * 마스터가 타 관리자 비밀번호를 재설정할 때 사용하는 임시 비밀번호 생성기.
 */
@Component
public class TemporaryPasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] password = new char[PASSWORD_LENGTH];
        password[0] = randomChar(UPPER);
        password[1] = randomChar(LOWER);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);

        String all = UPPER + LOWER + DIGITS + SPECIAL;
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password[i] = randomChar(all);
        }

        shuffle(password);
        return new String(password);
    }

    private char randomChar(String alphabet) {
        return alphabet.charAt(secureRandom.nextInt(alphabet.length()));
    }

    private void shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}
