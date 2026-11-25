package com.example.gamecommunity.config;

import com.example.gamecommunity.entity.Space;
import com.example.gamecommunity.repository.SpaceRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final SpaceRepository spaceRepository;

    public DataInitializer(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        createSpace(
                "종합 게임 공간",
                "general",
                "다양한 게임에 대한 이야기를 나누는 공간"
        );

        createSpace(
                "오버워치 공간",
                "overwatch",
                "오버워치 정보 및 플레이 경험 공유"
        );

        createSpace(
                "리그 오브 레전드 공간",
                "lol",
                "리그 오브 레전드 관련 정보 공유"
        );

        createSpace(
                "발로란트 공간",
                "valorant",
                "발로란트 전략 및 플레이 정보 공유"
        );

        createSpace(
                "스팀 게임 공간",
                "steam",
                "스팀 게임 추천 및 정보 공유"
        );

        createSpace(
                "자유 공간",
                "free",
                "자유로운 주제로 소통하는 공간"
        );
    }

    private void createSpace(String name,
                             String slug,
                             String description) {

        if (!spaceRepository.existsBySlug(slug)) {

            Space space = new Space(
                    name,
                    slug,
                    description
            );

            spaceRepository.save(space);
        }
    }
}