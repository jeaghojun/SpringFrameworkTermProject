package com.example.gamecommunity.service;
//회원가입 처리, 입력값 검사, 중복 검사, 비밀번호 해시 및 DB 저장을 담당
import com.example.gamecommunity.entity.Member;
import com.example.gamecommunity.repository.MemberRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String username,
                         String password,
                         String nickname) {

        username = username.trim();
        nickname = nickname.trim();

        // 아이디 유효성 검사
        if (!username.matches("[a-zA-Z0-9_]{4,30}")) {
            throw new IllegalArgumentException(
                    "아이디는 영문, 숫자, 밑줄을 사용하여 4~30자로 입력하세요."
            );
        }

        // 비밀번호 유효성 검사
        if (password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상, 72바이트 이하로 입력하세요."
            );
        }

        // 닉네임 유효성 검사
        if (nickname.length() < 2 || nickname.length() > 30) {
            throw new IllegalArgumentException(
                    "닉네임은 2~30자로 입력하세요."
            );
        }

        // 아이디 중복 검사
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다."
            );
        }

        // 닉네임 중복 검사
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        // 비밀번호 해시
        String encodedPassword =
                passwordEncoder.encode(password);

        // 회원 객체 생성
        Member member = new Member(
                username,
                encodedPassword,
                nickname
        );

        // MySQL에 저장
        memberRepository.save(member);
    }
}