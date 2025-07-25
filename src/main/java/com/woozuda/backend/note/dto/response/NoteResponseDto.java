package com.woozuda.backend.note.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteResponseDto {

    private Long id; //노트 ID
    private Long diaryId; //다이어리 ID
    private String diary; //다이어리 제목
    private String title; //노트 제목
    private String date; //노트 작성 날짜
    private String weather; //날씨 (RETROSPECTIVE type일 때는 null)
    private String season; //계절 (RETROSPECTIVE type일 때는 null)
    private String feeling; //감정 (RETROSPECTIVE type일 때는 null)
    private String question; // 질문 (COMMON,RETROSPECTIVE type일 때는 null)
    private String framework; //회고 프레임워크 (COMMON, QUESTION type일 때는 null)
    private List<String> content; //노트 내용(type에 상관없이 배열로 저장. COMMON,QUESTION type: content.size()==1, RETROSPECTIVE type: content.size()>1)

    @QueryProjection
    public NoteResponseDto(Long id, Long diaryId, String diary, String title, String date, String weather, String season, String feeling, List<String> content) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
        this.content = content;
    }

    @QueryProjection
    public NoteResponseDto(Long id, Long diaryId, String diary, String title, String date, String weather, String season, String feeling, String question, List<String> content) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
        this.question = question;
        this.content = content;
    }

    @QueryProjection
    public NoteResponseDto(Long id, Long diaryId, String diary, String title, String date, String framework, List<String> content) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.framework = framework;
        this.content = content;
    }

    @QueryProjection
    public NoteResponseDto(Long id, Long diaryId, String diary, String title, String date, String weather, String season, String feeling, String question, String framework, List<String> content) {
        this.id = id;
        this.diaryId = diaryId;
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
        this.question = question;
        this.framework = framework;
        this.content = content;
    }

    public static NoteResponseDto from(NoteResponseRepoDtoInterface repoDto) {
        return new NoteResponseDto(
                repoDto.getNoteId(),
                repoDto.getDiaryId(),
                repoDto.getDiaryTitle(),
                repoDto.getNoteTitle(),
                repoDto.getDate() != null ?  repoDto.getDate().toString() : null,
                repoDto.getWeather() != null ? repoDto.getWeather().getName() : null,
                repoDto.getSeason() != null ? repoDto.getSeason().getName() : null,
                repoDto.getFeeling() != null ? repoDto.getFeeling().getName() : null,
                repoDto.getQuestion(),
                repoDto.getFramework() != null ? repoDto.getFramework().name() : null,
                new ArrayList<>(List.of(repoDto.getContent()))
        );
    }

    public void addContent(String content) {
        this.content.add(content);
    }
    //TODO DTO가 도메인에 의존 -> 리팩토링

    public NoteResponseDto convertEnum() {
        if (this.weather != null) {
            this.weather = Weather.fromValue(this.weather);
        }
        if (this.season != null) {
            this.season = Season.fromValue(this.season);
        }
        if (this.feeling != null) {
            this.feeling = Feeling.fromValue(this.feeling);
        }
        return this;
    }
}
