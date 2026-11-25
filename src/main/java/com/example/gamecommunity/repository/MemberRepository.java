package com.example.gamecommunity.repository;

//회원가입, 로그인, 아이디 및 닉네임 중복 검사에 사용

import com.example.gamecommunity.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);
}