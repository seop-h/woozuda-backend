package com.woozuda.backend.ai_recall.repository.scs;

import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_recall.dto.Airecll_Scs_DTO;
import com.woozuda.backend.ai_recall.entity.Airecall_scs;
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
class AiRecallscsRpositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiRecallscsRpository aiRecallscsRpository;


    UserEntity user;
    Airecall_scs airecall_scs;


    @BeforeEach
    void setUp() {
        // User 엔티티 준비 및 저장
        user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_NOVEL, true, "sun@gmail.com", "woozuda");
        userRepository.save(user);

        // AiDiaryDTO 객체 생성
        Airecll_Scs_DTO airecall_scs_dto = new Airecll_Scs_DTO(
                "scs",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 7),
                "시점 회고 내용의 핵심 요약",
                "시작 시점에서의 강점 또는 긍정적 측면 분석",
                "시작 시점 개선할 점 또는 제안.",
                "진행 중 회고 내용의 핵심 요약.",
                "진행 중 강점 또는 긍정적 측면 분석.",
                "진행 중 개선할 점 또는 제안.",
                "종료 시점 회고 내용 요약.",
                "종료 시점 강점 또는 긍정적 측면 분석.",
                "종료 시점 개선할 점 또는 제안.",
                "시작 시점 개선 계획.",
                "진행 중 개선 계획.",
                "종료 시점 개선 계획.",
                user.getUsername()
        );

        // AiDiaryDTO를 AiDiary 엔티티로 변환
        airecall_scs = Airecall_scs.toairecallscsEntity(airecall_scs_dto, user);

        airecall_scs = aiRecallscsRpository.save(airecall_scs);
    }

    @DisplayName("AI scs 분석 결과 보기")
    @Test
    void scs리포트분석() {
        Airecall_scs found = aiRecallscsRpository.findById(airecall_scs.getAir_id())
                .orElseThrow(() -> new RuntimeException("데이터를 찾을 수 없습니다."));

        assertNotNull(found, "저장된 데이터가 null입니다.");
        assertEquals("scs", found.getType(), "SCS 타입이 다릅니다.");
        assertEquals(LocalDate.of(2024, 2, 1), found.getStart_date(), "시작일자가 다릅니다.");
        assertEquals(LocalDate.of(2024, 2, 7), found.getEnd_date(), "종료일자가 다릅니다.");

    }

}