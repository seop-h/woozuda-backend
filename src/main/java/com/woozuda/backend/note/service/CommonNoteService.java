package com.woozuda.backend.note.service;

import com.woozuda.backend.alarm.service.AlarmService;
import com.woozuda.backend.diary.dto.response.NoteIdResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
import com.woozuda.backend.note.dto.request.CommonNoteModifyRequestDto;
import com.woozuda.backend.note.dto.request.CommonNoteSaveRequestDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.entity.CommonNote;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.note.entity.NoteContent;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;
import com.woozuda.backend.note.repository.CommonNoteRepository;
import com.woozuda.backend.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static com.woozuda.backend.note.entity.type.Visibility.PRIVATE;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonNoteService {

    private final CommonNoteRepository commonNoteRepository;
    private final NoteRepository noteRepository;
    private final DiaryRepository diaryRepository;
    private final AlarmService alarmService;
    private final ImageService imageService;

    public NoteIdResponseDto saveCommonNote(String username, CommonNoteSaveRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }
        CommonNote commonNote = CommonNote.of(foundDiary,
                requestDto.getTitle(),
                LocalDate.parse(requestDto.getDate()),
                PRIVATE,
                Feeling.fromName(requestDto.getFeeling()),
                Weather.fromName(requestDto.getWeather()),
                Season.fromName(requestDto.getSeason())
        );
        CommonNote savedCommonNote = noteRepository.save(commonNote);

        NoteContent noteContent = NoteContent.of(1, requestDto.getContent());
        savedCommonNote.addContent(noteContent);

//        foundDiary.addNote(savedCommonNote.getDate());

        // 이번에 저장한 자유일기가 그 주의 3번째 일기라면(자유일기 + 질문일기 기준), 알람을 발생합니다.
        alarmService.threePostAlarm(username, requestDto.getDate());

        // 이미지 테이블 반영 (자유일기 생성 후)
        imageService.afterCreate(ImageType.NOTE, savedCommonNote.getId(), requestDto.getContent());

        return NoteIdResponseDto.of(savedCommonNote.getId());
    }

    //TODO 조회하는 노트가 로그인한 사용자의 노트인지 확인
    @Transactional(readOnly = true)
    public NoteResponseDto getCommonNote(String username, Long noteId) {
        NoteResponseDto responseDto = noteRepository.searchCommonNote(noteId);
        return responseDto.convertEnum();
    }

    public NoteIdResponseDto updateCommonNote(String username, Long noteId, CommonNoteModifyRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }

        CommonNote foundNote = commonNoteRepository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException("Note not found"));
        foundNote.update(
                foundDiary,
                requestDto.getTitle(),
                Weather.fromName(requestDto.getWeather()),
                Season.fromName(requestDto.getSeason()),
                Feeling.fromName(requestDto.getFeeling()),
                LocalDate.parse(requestDto.getDate()),
                requestDto.getContent()
        );

        // 이미지 테이블 반영 (자유일기 변경 후)
        imageService.afterUpdate(ImageType.NOTE, noteId, requestDto.getContent());

        return NoteIdResponseDto.of(foundNote.getId());
    }
}
