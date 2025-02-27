package com.woozuda.backend.ai_recall.repository.fourfs;

import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_recall.dto.Airecall_4fs_DTO;
import com.woozuda.backend.ai_recall.entity.Airecall_4fs;
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
class AiRecall4fsRpositoryTest {
    @Autowired
    private AiRecall4fsRpository aiRecall4fsRpository;

    @Autowired
    private UserRepository userRepository;


    UserEntity user;
    Airecall_4fs airecall_4fs;

    @BeforeEach
    void setUp() {
        // User 엔티티 준비 및 저장
        user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_NOVEL, true, "sun@gmail.com", "woozuda");
        userRepository.save(user);

        // AiDiaryDTO 객체 생성
        Airecall_4fs_DTO airecall_4fs_dto  = new Airecall_4fs_DTO(
                "ffs",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 7),
                "패턴 분석 내용",                         // patternAnalysis
                "긍정적인 행동 예시",                     // positiveBehavior
                "개선 제안 내용",                         // improvementSuggest
                "활용 팁 내용",                          // utilizationTips
                user.getUsername()
        );

        // AiDiaryDTO를 AiDiary 엔티티로 변환
        airecall_4fs = Airecall_4fs.toairecall4fsEntity(airecall_4fs_dto, user);

        airecall_4fs = aiRecall4fsRpository.save(airecall_4fs);
    }

    @DisplayName("AI 4fs분석 결과 보기")
    @Test
    void FFS(){
        Airecall_4fs found = aiRecall4fsRpository.findById(airecall_4fs.getAir_id()).orElseThrow(() -> new RuntimeException("데이터를 찾을 수 없습니다."));

        // 저장된 값들과 일치하는지 assertEquals로 검증
        assertNotNull(found, "저장된 데이터가 null입니다.");
        assertEquals(airecall_4fs.getStart_date(), found.getStart_date(), "시작 날짜가 일치하지 않습니다.");
        assertEquals(airecall_4fs.getEnd_date(), found.getEnd_date(), "끝 날짜가 일치하지 않습니다.");
        assertEquals(airecall_4fs.getPatternAnalysis(), found.getPatternAnalysis(), "패턴 분석 내용이 일치하지 않습니다.");
        assertEquals(airecall_4fs.getPositiveBehavior(), found.getPositiveBehavior(), "긍정적인 행동 예시가 일치하지 않습니다.");
        assertEquals(airecall_4fs.getImprovementSuggest(), found.getImprovementSuggest(), "개선 제안 내용이 일치하지 않습니다.");
        assertEquals(airecall_4fs.getUtilizationTips(), found.getUtilizationTips(), "활용 팁 내용이 일치하지 않습니다.");
    }
}