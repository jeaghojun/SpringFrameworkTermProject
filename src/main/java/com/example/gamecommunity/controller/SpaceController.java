package com.example.gamecommunity.controller;

import com.example.gamecommunity.entity.Space;
import com.example.gamecommunity.entity.Post;

import com.example.gamecommunity.repository.SpaceRepository;
import com.example.gamecommunity.repository.PostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SpaceController {

    private static final int PAGE_SIZE = 20;

    private final SpaceRepository spaceRepository;
    private final PostRepository postRepository;

    public SpaceController(
            SpaceRepository spaceRepository,
            PostRepository postRepository) {

        this.spaceRepository = spaceRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/spaces/{slug}")
    public String spacePage(

            @PathVariable("slug")
            String slug,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            Model model,
            RedirectAttributes redirectAttributes) {

        // 게임 공간 조회
        Space space = spaceRepository
                .findBySlug(slug)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 공간입니다."
                        ));

        // 페이지 번호 검사
        page = Math.max(page, 0);

        if (page > Integer.MAX_VALUE / PAGE_SIZE) {
            page = 0;
        }

        // 검색어 검사
        keyword = keyword.trim();

        if (keyword.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "검색어는 100자 이하로 입력해주세요."
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Post> posts;

        // 일반 게시글 목록
        if (keyword.isEmpty()) {

            posts = postRepository
                    .findBySpace_IdOrderByCreatedAtDesc(
                            space.getId(),
                            pageable
                    );

        } else {

            // 제목 검색
            posts = postRepository
                    .findBySpace_IdAndTitleContainingOrderByCreatedAtDesc(
                            space.getId(),
                            keyword,
                            pageable
                    );
        }

        // 전체 페이지 수
        int totalPages = Math.max(
                posts.getTotalPages(), 1
        );

        // 존재하지 않는 페이지 접근 방지
        if (page >= totalPages) {

            redirectAttributes.addAttribute(
                    "slug", space.getSlug()
            );

            if (!keyword.isEmpty()) {
                redirectAttributes.addAttribute(
                        "keyword", keyword
                );
            }

            return "redirect:/spaces/{slug}";
        }

        // 표시할 페이지 번호 범위
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(
                totalPages - 1, page + 2
        );

        // 화면 데이터 전달
        model.addAttribute("space", space);

        model.addAttribute(
                "spaces",
                spaceRepository.findAll(Sort.by("id"))
        );

        model.addAttribute(
                "posts",
                posts.getContent()
        );

        model.addAttribute(
                "postCount",
                posts.getTotalElements()
        );

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("keyword", keyword);

        return "space";
    }
}