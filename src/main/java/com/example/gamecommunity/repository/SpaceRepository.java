package com.example.gamecommunity.repository;

//게임별 공간(카테고리) 정보를 조회

import com.example.gamecommunity.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceRepository
        extends JpaRepository<Space, Long> {

    Optional<Space> findBySlug(String slug);

    boolean existsBySlug(String slug);
}