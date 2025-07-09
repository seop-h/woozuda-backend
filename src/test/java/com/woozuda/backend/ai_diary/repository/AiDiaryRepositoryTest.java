package com.woozuda.backend.ai_diary.repository;


import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_diary.dto.AiDiaryDTO;
import com.woozuda.backend.ai_diary.entity.AiDiary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AiDiaryRepositoryTest {
    @Autowired
    private AiDiaryRepository aiDiaryRepository;

    @Autowired
    private UserRepository userRepository;


    UserEntity user;
    AiDiary aiDiary;

    @BeforeEach
    void setUp() {
        // User 엔티티 준비 및 저장
        user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_NOVEL, true, "sun@gmail.com", "woozuda");
        userRepository.save(user);

        // AiDiaryDTO 객체 생성
        AiDiaryDTO aiDiaryDTO = new AiDiaryDTO(
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 7),
                "도서관",
                "독서",
                "기쁨",
                "맑음",
                0.6f,
                0.4f,
                0.8f,
                0.2f,
                "더 자주 독서 시간을 가져보세요!",
                user.getUsername()
        );

        // AiDiaryDTO를 AiDiary 엔티티로 변환
        aiDiary = AiDiary.toEntity(aiDiaryDTO, user);

        aiDiary = aiDiaryRepository.save(aiDiary);
    }

    @DisplayName("AI 분석 결과 보기")
    @Test
    void AIDiary() {
        // when
        AiDiary result = aiDiaryRepository.findByAiDiary(aiDiary.getStart_date(),aiDiary.getEnd_date() , aiDiary.getUser().getUsername())
                .orElseThrow(() -> new RuntimeException("AiDiary not found"));

        // then
        assertNotNull(result, "AI 분석 결과는 null이어서는 안됩니다.");

        assertEquals(aiDiary.getPlace(), result.getPlace(), "장소가 일치하지 않습니다.");
        assertEquals(aiDiary.getEmotion(), result.getEmotion(), "감정이 일치하지 않습니다.");
        assertEquals(aiDiary.getWeather(), result.getWeather(), "날씨가 일치하지 않습니다.");
        assertEquals(aiDiary.getWeekdayAt(), result.getWeekdayAt(), 0.001f, "평일 비율이 일치하지 않습니다.");
        assertEquals(aiDiary.getWeekendAt(), result.getWeekendAt(), 0.001f, "주말 비율이 일치하지 않습니다.");
        assertEquals(aiDiary.getPositive(), result.getPositive(), 0.001f, "긍정 비율이 일치하지 않습니다.");
        assertEquals(aiDiary.getDenial(), result.getDenial(), 0.001f, "부정 비율이 일치하지 않습니다.");
        assertEquals(aiDiary.getSuggestion(), result.getSuggestion(), "추천 내용이 일치하지 않습니다.");
        assertEquals(aiDiary.getUser().getUsername(), result.getUser().getUsername(), "사용자명이 일치하지 않습니다.");
    }
}