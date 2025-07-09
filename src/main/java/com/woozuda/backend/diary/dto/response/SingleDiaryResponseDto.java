package com.woozuda.backend.diary.dto.response;

import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.entity.DiaryTag;
import com.woozuda.backend.note.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleDiaryResponseDto {

    private Long id;
    private String title;
    private List<String> subject;
    private String imgUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer noteCount;

    public static SingleDiaryResponseDto of(
            Long id,
            String title,
            List<String> subject,
            String imgUrl,
            LocalDate startDate,
            LocalDate endDate,
            Integer noteCount
    ) {
        return new SingleDiaryResponseDto(id, title, subject, imgUrl, startDate, endDate, noteCount);
    }

    public static SingleDiaryResponseDto from(Diary diary) {
        List<Note> noteList = diary.getNoteList();
        noteList.sort(Comparator.comparing(Note::getDate).reversed());

        List<String> tagNames = diary.getTagList().stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .toList();

        return new SingleDiaryResponseDto(
                diary.getId(),
                diary.getTitle(),
                tagNames,
                diary.getImage(),
                !noteList.isEmpty() ? noteList.getFirst().getDate() : null,
                !noteList.isEmpty() ? noteList.getLast().getDate() : null,
                noteList.size()
        );
    }
}
