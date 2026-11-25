package com.example.gamecommunity.service;

import com.example.gamecommunity.entity.Comment;
import com.example.gamecommunity.entity.Member;
import com.example.gamecommunity.entity.Post;

import com.example.gamecommunity.repository.CommentRepository;
import com.example.gamecommunity.repository.MemberRepository;
import com.example.gamecommunity.repository.PostRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {

        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 댓글 작성
    @Transactional
    public void createComment(
            Long postId,
            String content,
            String nickname,
            String password,
            String username) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 게시글입니다."
                        ));

        content = content.trim();

        // 댓글 내용 검증
        if (content.isEmpty() || content.length() > 1000) {

            throw new IllegalArgumentException(
                    "댓글은 1~1000자로 입력해주세요."
            );
        }

        Member member = null;
        String passwordHash = null;

        // 회원 댓글
        if (username != null) {

            member = memberRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.UNAUTHORIZED
                            ));

            nickname = member.getNickname();

        } else {

            // 비회원 댓글
            nickname = nickname.trim();

            if (nickname.length() < 2
                    || nickname.length() > 30) {

                throw new IllegalArgumentException(
                        "닉네임은 2~30자로 입력해주세요."
                );
            }

            if (password.length() < 8
                    || password.getBytes(
                    StandardCharsets.UTF_8).length > 72) {

                throw new IllegalArgumentException(
                        "비밀번호는 8자 이상, UTF-8 기준 72바이트 이하로 입력해주세요."
                );
            }

            passwordHash =
                    passwordEncoder.encode(password);
        }

        Comment comment = new Comment(
                post,
                member,
                nickname,
                passwordHash,
                content
        );

        commentRepository.save(comment);
    }

    // 회원 댓글 작성자 확인
    private boolean isOwner(
            Comment comment,
            String username) {

        return username != null
                && comment.getMember() != null
                && username.equals(
                comment.getMember().getUsername()
        );
    }

    // 게시글별 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentDetail> getComments(
            Long postId,
            String username) {

        return commentRepository
                .findByPost_IdOrderByCreatedAtAsc(postId)
                .stream()
                .map(comment -> {

                    boolean guestComment =
                            comment.getMember() == null;

                    boolean canDelete =
                            guestComment
                                    || isOwner(comment, username);

                    return new CommentDetail(
                            comment.getId(),
                            comment.getNickname(),
                            comment.getContent(),
                            comment.getCreatedAt(),
                            guestComment,
                            canDelete
                    );
                })
                .toList();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(
            Long postId,
            Long commentId,
            String username,
            String password) {

        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 댓글입니다."
                        ));

        // 게시글과 댓글의 연결 관계 확인
        if (!comment.getPost().getId().equals(postId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 게시글의 댓글이 아닙니다."
            );
        }

        // 회원 댓글 삭제 권한 검사
        if (comment.getMember() != null) {

            if (!isOwner(comment, username)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "댓글 작성자만 삭제할 수 있습니다."
                );
            }

        } else {

            // 비회원 댓글 비밀번호 검사
            if (password == null
                    || password.isEmpty()
                    || password.getBytes(
                    StandardCharsets.UTF_8).length > 72
                    || comment.getPasswordHash() == null
                    || !passwordEncoder.matches(
                    password,
                    comment.getPasswordHash())) {

                throw new IllegalArgumentException(
                        "댓글 비밀번호가 올바르지 않습니다."
                );
            }
        }

        commentRepository.delete(comment);
    }

    // 화면 출력용 DTO
    public record CommentDetail(
            Long id,
            String nickname,
            String content,
            LocalDateTime createdAt,
            boolean guestComment,
            boolean canDelete
    ) {
    }
}