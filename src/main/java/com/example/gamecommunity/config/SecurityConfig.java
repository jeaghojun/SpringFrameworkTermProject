package com.example.gamecommunity.config;

// Spring Security 인증 및 접근 권한 설정

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // 기본 페이지 및 정적 리소스
                        .requestMatchers(
                                "/",
                                "/register",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error"
                        ).permitAll()

                        // 게시글 및 게임 공간 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/spaces/*",
                                "/spaces/*/write",
                                "/posts/*",
                                "/posts/*/edit"
                        ).permitAll()

                        // 게시글, 댓글 및 추천 요청
                        .requestMatchers(
                                HttpMethod.POST,
                                "/spaces/*/write",
                                "/posts/*/edit",
                                "/posts/*/delete",
                                "/posts/*/comments",
                                "/posts/*/comments/*/delete",
                                "/posts/*/like"
                        ).permitAll()

                        // 나머지 요청은 로그인 필요
                        .anyRequest().authenticated()
                )

                // 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}