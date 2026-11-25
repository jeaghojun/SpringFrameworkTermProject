package com.example.gamecommunity.service;

//로그인 시 MySQL에서 회원 정보를 불러오는 클래스

import com.example.gamecommunity.entity.Member;
import com.example.gamecommunity.repository.MemberRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(
            MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Member member = memberRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        return User.builder()
                .username(member.getUsername())
                .password(member.getPasswordHash())
                .roles(member.getRole())
                .build();
    }
}