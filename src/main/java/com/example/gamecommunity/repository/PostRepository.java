package com.example.gamecommunity.repository;

import com.example.gamecommunity.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository
        extends JpaRepository<Post, Long> {

    // 공간별 게시글 조회
    Page<Post> findBySpace_IdOrderByCreatedAtDesc(
            Long spaceId,
            Pageable pageable
    );

    // 공간별 게시글 제목 검색
    Page<Post> findBySpace_IdAndTitleContainingOrderByCreatedAtDesc(
            Long spaceId,
            String keyword,
            Pageable pageable
    );

    // 최신 게시글 10개
    List<Post> findTop10ByOrderByCreatedAtDesc();

    // 인기 게시글 10개
    List<Post> findTop10ByOrderByLikeCountDescCreatedAtDesc();

    // 조회수 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Post p
            SET p.viewCount = p.viewCount + 1
            WHERE p.id = :id
            """)
    int increaseViewCount(@Param("id") Long id);

    // 추천수 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Post p
            SET p.likeCount = p.likeCount + 1
            WHERE p.id = :id
            """)
    int increaseLikeCount(@Param("id") Long id);
}