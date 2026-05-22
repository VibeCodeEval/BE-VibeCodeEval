package com.yd.vibecode.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class TemporaryPasswordGeneratorTest {

    private final TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();

    @RepeatedTest(20)
    @DisplayName("임시 비밀번호는 12자이며 영문 대/소문자, 숫자, 특수문자를 포함한다")
    void generate_meetsPolicy() {
        String password = generator.generate();

        assertThat(password).hasSize(12);
        assertThat(password).matches(".*[A-Z].*");
        assertThat(password).matches(".*[a-z].*");
        assertThat(password).matches(".*[0-9].*");
        assertThat(password).matches(".*[!@#$%&*].*");
    }

    @Test
    @DisplayName("연속 생성 시 서로 다른 비밀번호가 나온다")
    void generate_isNotConstant() {
        String first = generator.generate();
        String second = generator.generate();

        assertThat(first).isNotEqualTo(second);
    }
}
