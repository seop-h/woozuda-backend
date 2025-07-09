package com.woozuda.backend.ai_recall.repository.kpt;

import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_recall.dto.Airecall_Kpt_DTO;
import com.woozuda.backend.ai_recall.entity.Airecall_kpt;
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
class AiRecallkptRpositoryTest {
    @Autowired
    private AiRecallkptRpository aiRecallkptRpository;

    @Autowired
    private UserRepository userRepository;


    UserEntity user;
    Airecall_kpt airecall_kpt;

    @BeforeEach
    void setUp() {
        user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_NOVEL, true, "sun@gmail.com", "woozuda");
        userRepository.save(user);

        // AiDiaryDTO 객체 생성
        Airecall_Kpt_DTO airecall_kpt_dto  = new Airecall_Kpt_DTO(
                "kpt",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 7),
                "강점 분석",
                "개선 제안",
                "개선 제안 내용",
                user.getUsername()
        );

        // AiDiaryDTO를 AiDiary 엔티티로 변환
        airecall_kpt = Airecall_kpt.toairecallktpEntity(airecall_kpt_dto, user);

        airecall_kpt = aiRecallkptRpository.save(airecall_kpt);
    }

    @DisplayName("AI KPT 분석 결과 보기")
    @Test
    void kpt(){
        Airecall_kpt found = aiRecallkptRpository.findById(airecall_kpt.getAir_id()).orElseThrow(() -> new RuntimeException("데이터를 찾을 수 없습니다."));

        // 저장된 값들과 일치하는지 assertEquals로 검증
        assertNotNull(found, "저장된 데이터가 null입니다.");
        assertEquals(airecall_kpt.getStart_date(), found.getStart_date(), "시작 날짜가 일치하지 않습니다.");
        assertEquals(airecall_kpt.getEnd_date(), found.getEnd_date(), "끝 날짜가 일치하지 않습니다.");

    }
}