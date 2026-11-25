package com.example.gamecommunity.controller;

import com.example.gamecommunity.service.MemberService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원가입 페이지
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("nickname") String nickname,
            Model model) {

        try {
            memberService.register(
                    username, password, nickname
            );

            return "redirect:/login?registered";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            return "register";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "error",
                    "이미 사용 중인 아이디 또는 닉네임입니다."
            );

            return "register";
        }
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 로그인 성공 확인 페이지
    @GetMapping("/account")
    public String accountPage(
            Principal principal,
            Model model) {

        model.addAttribute(
                "username",
                principal.getName()
        );

        return "account";
    }
}