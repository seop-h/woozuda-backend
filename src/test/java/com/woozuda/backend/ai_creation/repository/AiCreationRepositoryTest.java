package com.woozuda.backend.ai_creation.repository;

import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_creation.dto.AiCreationDTO;
import com.woozuda.backend.ai_creation.entity.AiCreation;
import com.woozuda.backend.ai_creation.entity.CreationType;
import com.woozuda.backend.ai_creation.entity.CreationVisibility;
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
class AiCreationRepositoryTest {
    @Autowired
    private AiCreationRepository aiCreationRepository;

    @Autowired
    private UserRepository userRepository;

    UserEntity user;
    AiCreation aiCreation;

    @BeforeEach
    void setUp() {
        user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_NOVEL, true, "sun@gmail.com", "woozuda");

        // 실패가 떠야함
        //user = new UserEntity(null, "sun", "sun0304@", "ROLE_USER", AiType.PICTURE_POETRY, true, "sun@gmail.com", "woozuda");
        userRepository.save(user);

        // AiType이 PICTURE_NOVEL일 때만 CreationType을 설정
        AiCreationDTO aiCreationDTO;
        if (user.getAiType() == AiType.PICTURE_NOVEL) {

            aiCreationDTO = new AiCreationDTO(
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 2, 7),
                    CreationType.POETRY.name(), // POETRY일 때 통과
                    "image_url",
                    "text",
                    CreationVisibility.PUBLIC.name(),
                    user.getUsername()
            );

            aiCreation = AiCreation.toCreationEntity(aiCreationDTO, user);
            aiCreationRepository.save(aiCreation);
        }
    }

    @DisplayName("AI 창작 분석")
    @Test
    void AICreation() {
        // 주어진 아이디로 AI 창작을 찾기
        AiCreation found = aiCreationRepository.findById(aiCreation.getAi_creation_id())
                .orElseThrow(() -> new RuntimeException("AI 창작 데이터를 찾을 수 없습니다."));

        // 검증: 저장된 엔티티가 null이 아님을 확인
        assertNotNull(found, "저장된 AI 창작 데이터가 null입니다.");

        // 저장된 AI 창작 데이터가 예상대로 저장되었는지 검증
        assertEquals(aiCreation.getCreationType(), found.getCreationType(), "Creation type이 일치하지 않습니다.");
        assertEquals(aiCreation.getImage_url(), found.getImage_url(), "Image URL이 일치하지 않습니다.");
        assertEquals(aiCreation.getText(), found.getText(), "Text가 일치하지 않습니다.");
        assertEquals(aiCreation.getCreationVisibility(), found.getCreationVisibility(), "Visibility가 일치하지 않습니다.");
        assertEquals(user.getUsername(), found.getUser().getUsername(), "User가 일치하지 않습니다.");
    }

}