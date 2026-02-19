package com.alrimjang.service.impl;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.RegisterRequest;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Users user = Users.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .build();

        userMapper.insertUser(user);
    }
}
