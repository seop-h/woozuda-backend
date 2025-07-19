package com.woozuda.backend.note.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Locale;
import java.util.Map;

@NoArgsConstructor
@Getter
@ToString
public class NoteEntryResponseDto implements Comparable<NoteEntryResponseDto> {

    private static final Map<String, Integer> priority = Map.of("common", 1, "question", 2, "retrospective", 3);

    private String type; //노트 종류 (common(COMMON), question(QUESTION), retrospective(RETROSPECTIVE))
    private NoteResponseDto note;

    /**
     * 이 생성자에서 노트 종류를 소문자로 변환
     * @param type 노트 종료 (대문자)
     * @param note
     */
    @QueryProjection
    public NoteEntryResponseDto(String type, NoteResponseDto note) {
        this.type = type.toLowerCase(Locale.ROOT);
        this.note = note;
    }

    /*
    졍렬 기준
    1. 날짜 - 최근 순
    2. 일기 종류 - 자유일기, 오늘의 질문 일기, 회고 순
    */
    @Override
    public int compareTo(NoteEntryResponseDto o) {
        int first = o.getNote().getDate().compareTo(this.getNote().getDate());
        if (first != 0) {
            return first;
        }

        return priority.get(this.getType()) - priority.get(o.getType());
    }
}
