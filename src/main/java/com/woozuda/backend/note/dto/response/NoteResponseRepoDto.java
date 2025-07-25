package com.woozuda.backend.note.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Framework;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NoteResponseRepoDto {

    private Long id; //노트 ID
    private Long diaryId; //다이어리 ID
    private String diary; //다이어리 제목
    private String title; //노트 제목
    private LocalDate date; //노트 작성 날짜
    private String type; //노트 타입(COMMON, QUESTION, RETROSPECTIVE)
    private Weather weather; //날씨 (RETROSPECTIVE type일 때는 null)
    private Season season; //계절 (RETROSPECTIVE type일 때는 null)
    private Feeling feeling; //감정 (RETROSPECTIVE type일 때는 null)
    private String question; // 질문 (COMMON, RETROSPECTIVE type일 때는 null)
    private Framework framework; //회고 프레임워크 (COMMON, QUESTION type일 때는 null)
    private String content; //노트 내용
    private Integer order; //노트 내용 순서(COMMON,QUESTION type: 1, RETROSPECTIVE type: 1~4)

    //common
    @QueryProjection
    public NoteResponseRepoDto(Long id, Long diaryId, String diary, String title, LocalDate date, String type, Weather weather, Season season, Feeling feeling, String content, Integer order) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.type = type;
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
        this.content = content;
        this.order = order;
    }

    //question
    @QueryProjection
    public NoteResponseRepoDto(Long id, Long diaryId, String diary, String title, LocalDate date, String type, Weather weather, Season season, Feeling feeling, String question, String content, Integer order) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.type = type;
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
        this.question = question;
        this.content = content;
        this.order = order;
    }


    //retrospective
    @QueryProjection
    public NoteResponseRepoDto(Long id, Long diaryId, String diary, String title, LocalDate date, String type, Framework framework, String content, Integer order) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.type = type;
        this.framework = framework;
        this.content = content;
        this.order = order;
    }
}
