package com.woozuda.backend.ai_diary.entity;


import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.ai_diary.dto.AiDiaryDTO;
import com.woozuda.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "ai_diary_rep")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiDiary extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aid_id")
    private Long id;

    //유저 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate start_date;

    @Column(nullable = false)
    private LocalDate end_date;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String place;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String activity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String emotion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String weather;

    @Column(nullable = false)
    private float weekdayAt;

    @Column(nullable = false)
    private float weekendAt;

    @Column(nullable = false)
    private float positive;

    @Column(nullable = false)
    private float denial;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String suggestion;


    // 생성자 주입 방식
    public static AiDiary toEntity(AiDiaryDTO aiDiaryDTO, UserEntity username) {
        return new AiDiary(
                null,  // id는 null로 설정 (자동 생성되도록)
                username,  // UserID
                aiDiaryDTO.getStart_date(),
                aiDiaryDTO.getEnd_date(),
                aiDiaryDTO.getPlace(),
                aiDiaryDTO.getActivity(),
                aiDiaryDTO.getEmotion(),
                aiDiaryDTO.getWeather(),
                aiDiaryDTO.getWeekdayAt(),
                aiDiaryDTO.getWeekendAt(),
                aiDiaryDTO.getPositive(),
                aiDiaryDTO.getDenial(),
                aiDiaryDTO.getSuggestion()
        );
    }
}
