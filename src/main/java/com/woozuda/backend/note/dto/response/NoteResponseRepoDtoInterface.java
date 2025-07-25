package com.woozuda.backend.note.dto.response;

import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Framework;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;

import java.time.LocalDate;

public interface NoteResponseRepoDtoInterface {

    Long getDiaryId();
    String getDiaryTitle();
    Long getNoteId();
    String getNoteTitle(); //노트 제목
    LocalDate getDate(); //노트 작성 날짜
    String getContent(); //노트 내용
    String getType(); //노트 타입(COMMON, QUESTION, RETROSPECTIVE)
    Feeling getFeeling(); //감정 (RETROSPECTIVE type일 때는 null)
    Weather getWeather(); //날씨 (RETROSPECTIVE type일 때는 null)
    Season getSeason(); //계절 (RETROSPECTIVE type일 때는 null)
    String getQuestion(); // 질문 (COMMON, RETROSPECTIVE type일 때는 null)
    Framework getFramework(); //회고 프레임워크 (COMMON, QUESTION type일 때는 null)
    Integer getContentOrder(); //노트 내용 순서(COMMON,QUESTION type: 1, RETROSPECTIVE type: 1~4)

}
