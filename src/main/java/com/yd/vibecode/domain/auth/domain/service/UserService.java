package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElse(null);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User create(String name, String phone) {
        User user = User.builder()
                .name(name)
                .phone(phone)
                .build();
        return userRepository.save(user);
    }
}

