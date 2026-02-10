package com.alrimjang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (H2 콘솔은 CSRF 토큰을 지원하지 않음)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/error"))

                // 2. HTTP 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용
                                .requestMatchers("/error").permitAll()         // 에러 페이지 허용
                                .anyRequest().permitAll()                      // 우선 모든 요청 허용 (테스트용)
                        // .anyRequest().authenticated()               // 나중에 보안이 필요하면 이걸로 변경
                )

                // 3. H2 콘솔이 <iframe>을 사용할 수 있도록 설정
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}