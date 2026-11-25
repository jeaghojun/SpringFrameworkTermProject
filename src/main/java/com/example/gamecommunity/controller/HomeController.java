package com.example.gamecommunity.controller;

import com.example.gamecommunity.repository.SpaceRepository;
import com.example.gamecommunity.repository.PostRepository;
import com.example.gamecommunity.repository.MemberRepository;
import com.example.gamecommunity.entity.Member;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final SpaceRepository spaceRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public HomeController(
            SpaceRepository spaceRepository,
            PostRepository postRepository,
            MemberRepository memberRepository) {

        this.spaceRepository = spaceRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {

        // 게임별 공간 목록
        model.addAttribute(
                "spaces",
                spaceRepository.findAll(Sort.by("id"))
        );

        // 최신 게시글 10개
        model.addAttribute(
                "latestPosts",
                postRepository.findTop10ByOrderByCreatedAtDesc()
        );

        // 인기 게시글 10개
        model.addAttribute(
                "popularPosts",
                postRepository
                        .findTop10ByOrderByLikeCountDescCreatedAtDesc()
        );

        // 커뮤니티 현황
        model.addAttribute(
                "spaceCount",
                spaceRepository.count()
        );

        model.addAttribute(
                "postCount",
                postRepository.count()
        );

        model.addAttribute(
                "memberCount",
                memberRepository.count()
        );

        // 로그인 정보 확인
        if (principal != null) {

            String username = principal.getName();

            String nickname = memberRepository
                    .findByUsername(username)
                    .map(Member::getNickname)
                    .orElse(username);

            model.addAttribute("username", username);
            model.addAttribute("nickname", nickname);
        }

        return "index";
    }
}