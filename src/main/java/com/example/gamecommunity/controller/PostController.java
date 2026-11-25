package com.example.gamecommunity.controller;

import com.example.gamecommunity.service.PostService;
import com.example.gamecommunity.service.CommentService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(
            PostService postService,
            CommentService commentService) {

        this.postService = postService;
        this.commentService = commentService;
    }

    // 글쓰기 화면
    @GetMapping("/spaces/{slug}/write")
    public String writePage(
            @PathVariable("slug") String slug,
            Principal principal,
            Model model) {

        model.addAttribute(
                "space",
                postService.findSpace(slug)
        );

        model.addAttribute(
                "loggedIn",
                principal != null
        );

        return "post-write";
    }

    // 게시글 등록
    @PostMapping("/spaces/{slug}/write")
    public String createPost(
            @PathVariable("slug") String slug,

            @RequestParam(
                    name = "title",
                    defaultValue = "")
            String title,

            @RequestParam(
                    name = "content",
                    defaultValue = "")
            String content,

            @RequestParam(
                    name = "nickname",
                    defaultValue = "")
            String nickname,

            @RequestParam(
                    name = "password",
                    defaultValue = "")
            String password,

            Principal principal,
            Model model) {

        String username = principal != null
                ? principal.getName()
                : null;

        try {

            Long postId = postService.createPost(
                    slug,
                    title,
                    content,
                    nickname,
                    password,
                    username
            );

            return "redirect:/posts/" + postId;

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "space",
                    postService.findSpace(slug)
            );

            model.addAttribute(
                    "loggedIn",
                    principal != null
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("nickname", nickname);

            return "post-write";
        }
    }

    // 게시글 상세 화면 및 댓글 조회
    @GetMapping("/posts/{id}")
    public String postDetail(
            @PathVariable("id") Long id,
            Principal principal,
            HttpSession session,
            Model model) {

        String username = principal != null
                ? principal.getName()
                : null;

        // 게시글 조회 및 조회수 증가
        model.addAttribute(
                "post",
                postService.getPostAndIncreaseView(id)
        );

        // 게시글 수정 및 삭제 권한
        model.addAttribute(
                "manage",
                postService.getManageInfo(id, username)
        );

        // 댓글 목록 조회
        var comments =
                commentService.getComments(id, username);

        model.addAttribute(
                "comments",
                comments
        );

        // 댓글 개수
        model.addAttribute(
                "commentCount",
                comments.size()
        );

        // 로그인 여부
        model.addAttribute(
                "loggedIn",
                username != null
        );

        // 현재 세션의 추천 여부
        boolean liked = Boolean.TRUE.equals(
                session.getAttribute("likedPost:" + id)
        );

        model.addAttribute("liked", liked);

        return "post-detail";
    }

    // 게시글 수정 화면
    @GetMapping("/posts/{id}/edit")
    public String editPage(
            @PathVariable("id") Long id,
            Principal principal,
            Model model) {

        String username = principal != null
                ? principal.getName()
                : null;

        PostService.ManageInfo manage =
                postService.getManageInfo(id, username);

        // 작성자 권한 확인
        if (!manage.canManage()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "게시글 작성자만 수정할 수 있습니다."
            );
        }

        PostService.PostDetail post =
                postService.getPost(id);

        model.addAttribute("post", post);
        model.addAttribute("manage", manage);

        if (!model.containsAttribute("title")) {

            model.addAttribute(
                    "title",
                    post.title()
            );
        }

        if (!model.containsAttribute("content")) {

            model.addAttribute(
                    "content",
                    post.content()
            );
        }

        return "post-edit";
    }

    // 게시글 수정 처리
    @PostMapping("/posts/{id}/edit")
    public String updatePost(
            @PathVariable("id") Long id,

            @RequestParam(
                    name = "title",
                    defaultValue = "")
            String title,

            @RequestParam(
                    name = "content",
                    defaultValue = "")
            String content,

            @RequestParam(
                    name = "password",
                    defaultValue = "")
            String password,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal != null
                ? principal.getName()
                : null;

        try {

            postService.updatePost(
                    id,
                    title,
                    content,
                    username,
                    password
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "게시글이 수정되었습니다."
            );

            return "redirect:/posts/" + id;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "title",
                    title
            );

            redirectAttributes.addFlashAttribute(
                    "content",
                    content
            );

            return "redirect:/posts/" + id + "/edit";
        }
    }

    // 게시글 삭제 처리
    @PostMapping("/posts/{id}/delete")
    public String deletePost(
            @PathVariable("id") Long id,

            @RequestParam(
                    name = "password",
                    defaultValue = "")
            String password,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal != null
                ? principal.getName()
                : null;

        try {

            String slug = postService.deletePost(
                    id,
                    username,
                    password
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "게시글이 삭제되었습니다."
            );

            return "redirect:/spaces/" + slug;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/posts/" + id;
        }
    }

    // 게시글 추천
    @PostMapping("/posts/{id}/like")
    public synchronized String likePost(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String sessionKey = "likedPost:" + id;

        // 동일 세션의 중복 추천 검사
        if (Boolean.TRUE.equals(
                session.getAttribute(sessionKey))) {

            redirectAttributes.addFlashAttribute(
                    "likeMessage",
                    "이미 추천한 게시글입니다."
            );

            return "redirect:/posts/" + id;
        }

        // 추천수 증가
        postService.likePost(id);

        // 세션에 추천 기록 저장
        session.setAttribute(sessionKey, true);

        redirectAttributes.addFlashAttribute(
                "likeMessage",
                "게시글을 추천하였습니다."
        );

        return "redirect:/posts/" + id;
    }
}