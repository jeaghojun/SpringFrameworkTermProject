package com.example.gamecommunity.service;

import com.example.gamecommunity.repository.CommentRepository;
import com.example.gamecommunity.entity.Member;
import com.example.gamecommunity.entity.Post;
import com.example.gamecommunity.entity.Space;

import com.example.gamecommunity.repository.MemberRepository;
import com.example.gamecommunity.repository.PostRepository;
import com.example.gamecommunity.repository.SpaceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpSession;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SpaceRepository spaceRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;


    public PostService(
            PostRepository postRepository,
            SpaceRepository spaceRepository,
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            CommentRepository commentRepository) {

        this.postRepository = postRepository;
        this.spaceRepository = spaceRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.commentRepository = commentRepository;
    }

    // 게임 공간 조회
    @Transactional(readOnly = true)
    public Space findSpace(String slug) {

        return spaceRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 공간입니다."
                        ));
    }

    // 게시글 작성
    @Transactional
    public Long createPost(
            String slug,
            String title,
            String content,
            String nickname,
            String password,
            String username) {

        Space space = findSpace(slug);

        title = title.trim();
        content = content.trim();

        // 제목 검사
        if (title.isEmpty() || title.length() > 100) {
            throw new IllegalArgumentException(
                    "제목은 1~100자로 입력해주세요."
            );
        }

        // 내용 검사
        if (content.isEmpty() || content.length() > 10000) {
            throw new IllegalArgumentException(
                    "내용은 1~10000자로 입력해주세요."
            );
        }

        Member member = null;
        String passwordHash = null;

        // 회원 게시글
        if (username != null) {

            member = memberRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.UNAUTHORIZED
                            ));

            nickname = member.getNickname();

        } else {

            // 비회원 게시글
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

        // 게시글 저장
        Post post = new Post(
                space,
                member,
                title,
                content,
                nickname,
                passwordHash
        );

        return postRepository.save(post).getId();
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetail getPost(Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 게시글입니다."
                        ));

        return new PostDetail(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getNickname(),
                post.getCreatedAt(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getSpace().getSlug(),
                post.getSpace().getName()
        );
    }

    // 화면 출력용 데이터
    public record PostDetail(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt,
            int viewCount,
            int likeCount,
            String spaceSlug,
            String spaceName
    ) {
    }

    // 게시글 엔티티 조회
    private Post findPost(Long id) {

        return postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "존재하지 않는 게시글입니다."
                        ));
    }

    // 회원 게시글 작성자 확인
    private boolean isOwner(Post post, String username) {

        return username != null
                && post.getMember() != null
                && username.equals(
                post.getMember().getUsername()
        );
    }

    // 수정 및 삭제 버튼 표시 정보
    @Transactional(readOnly = true)
    public ManageInfo getManageInfo(
            Long id, String username) {

        Post post = findPost(id);

        boolean guestPost = post.getMember() == null;

        boolean canManage =
                guestPost || isOwner(post, username);

        return new ManageInfo(guestPost, canManage);
    }

    public record ManageInfo(
            boolean guestPost,
            boolean canManage
    ) {
    }

    // 서버에서 실제 수정 및 삭제 권한 검사
    private void validatePermission(
            Post post,
            String username,
            String password) {

        // 회원 게시글
        if (post.getMember() != null) {

            if (!isOwner(post, username)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "게시글 작성자만 수정하거나 삭제할 수 있습니다."
                );
            }

            return;
        }

        // 비회원 게시글
        if (password == null
                || password.isEmpty()
                || password.getBytes(
                StandardCharsets.UTF_8).length > 72
                || post.getPasswordHash() == null
                || !passwordEncoder.matches(
                password,
                post.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "게시글 비밀번호가 올바르지 않습니다."
            );
        }
    }

    // 게시글 수정
    @Transactional
    public void updatePost(
            Long id,
            String title,
            String content,
            String username,
            String password) {

        Post post = findPost(id);

        validatePermission(post, username, password);

        title = title.trim();
        content = content.trim();

        if (title.isEmpty() || title.length() > 100) {

            throw new IllegalArgumentException(
                    "제목은 1~100자로 입력해주세요."
            );
        }

        if (content.isEmpty() || content.length() > 10000) {

            throw new IllegalArgumentException(
                    "내용은 1~10000자로 입력해주세요."
            );
        }

        post.update(title, content);
    }

    // 게시글 삭제
    @Transactional
    public String deletePost(
            Long id,
            String username,
            String password) {

        Post post = findPost(id);

        validatePermission(post, username, password);

        String slug = post.getSpace().getSlug();

        // 연결된 댓글 먼저 삭제
        commentRepository.deleteByPost_Id(id);
        commentRepository.flush();

        // 게시글 삭제
        postRepository.delete(post);

        return slug;
    }

    // 게시글 상세 조회 및 조회수 증가
    @Transactional
    public PostDetail getPostAndIncreaseView(Long id) {

        int updated = postRepository.increaseViewCount(id);

        if (updated == 0) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 게시글입니다."
            );
        }

        return getPost(id);
    }

    // 게시글 추천수 증가
    @Transactional
    public void likePost(Long id) {

        int updated = postRepository.increaseLikeCount(id);

        if (updated == 0) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 게시글입니다."
            );
        }
    }
}