package com.yd.vibecode.domain.auth.domain.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 생성 성공")
    void create_success() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        User user = User.builder()
                .name(name)
                .phone(phone)
                .build();

        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        User result = userService.create(name, phone);

        // then
        assertThat(result.getName()).isEqualTo(name);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("전화번호로 사용자 조회")
    void findByPhone_success() {
        // given
        String phone = "010-1234-5678";
        User user = User.builder().phone(phone).build();
        given(userRepository.findByPhone(phone)).willReturn(Optional.of(user));

        // when
        User result = userService.findByPhone(phone);

        // then
        assertThat(result.getPhone()).isEqualTo(phone);
    }
}

