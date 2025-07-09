package com.woozuda.backend.ai_creation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woozuda.backend.ai_creation.entity.AiCreation;
import com.woozuda.backend.ai_diary.entity.AiDiary;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static com.woozuda.backend.ai_creation.entity.QAiCreation.aiCreation;
import static com.woozuda.backend.account.entity.QUserEntity.userEntity;
import static com.woozuda.backend.ai_diary.entity.QAiDiary.aiDiary;

public class AiCreationRepositoryImpl implements AiCreationRepositoryCustom{

    private final JPAQueryFactory query;

    public AiCreationRepositoryImpl(EntityManager entityManager) {
        this.query = new JPAQueryFactory(entityManager);
    }
    // 타입별로 가지고 와야 하나? .. 음,.,
    @Override
    public Optional<AiCreation> findByAiCreation(LocalDate start_date, LocalDate end_date, String username) {
        AiCreation result = query
                .selectFrom(aiCreation)
                .join(aiCreation.user, userEntity)
                .where(aiCreation.start_date.goe(start_date)  // 필드 이름을 실제 엔티티에 맞게 수정
                        .and(aiCreation.end_date.loe(end_date))
                        .and(userEntity.username.eq(username)))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
