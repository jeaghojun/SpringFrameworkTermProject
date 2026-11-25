package com.example.gamecommunity.controller;

import com.example.gamecommunity.service.CommentService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(
            CommentService commentService) {

        this.commentService = commentService;
    }

    // 댓글 작성
    @PostMapping("/posts/{id}/comments")
    public String createComment(
            @PathVariable("id") Long id,

            @RequestParam(name = "content", defaultValue = "")
            String content,

            @RequestParam(name = "nickname", defaultValue = "")
            String nickname,

            @RequestParam(name = "password", defaultValue = "")
            String password,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal != null
                ? principal.getName()
                : null;

        try {

            commentService.createComment(
                    id,
                    content,
                    nickname,
                    password,
                    username
            );

            redirectAttributes.addFlashAttribute(
                    "commentSuccess",
                    "댓글이 등록되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "commentError",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "commentContent",
                    content
            );

            redirectAttributes.addFlashAttribute(
                    "commentNickname",
                    nickname
            );
        }

        return "redirect:/posts/" + id + "#comments";
    }

    // 댓글 삭제
    @PostMapping("/posts/{id}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable("id") Long id,

            @PathVariable("commentId") Long commentId,

            @RequestParam(name = "password", defaultValue = "")
            String password,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal != null
                ? principal.getName()
                : null;

        try {

            commentService.deleteComment(
                    id,
                    commentId,
                    username,
                    password
            );

            redirectAttributes.addFlashAttribute(
                    "commentSuccess",
                    "댓글이 삭제되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "commentError",
                    e.getMessage()
            );
        }

        return "redirect:/posts/" + id + "#comments";
    }
}