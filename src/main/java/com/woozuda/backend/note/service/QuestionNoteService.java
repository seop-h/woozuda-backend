package com.woozuda.backend.note.service;

import com.woozuda.backend.alarm.service.AlarmService;
import com.woozuda.backend.diary.dto.response.NoteIdResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
import com.woozuda.backend.note.dto.request.QuestionNoteModifyRequestDto;
import com.woozuda.backend.note.dto.request.QuestionNoteSaveRequestDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.entity.NoteContent;
import com.woozuda.backend.question.entity.Question;
import com.woozuda.backend.note.entity.QuestionNote;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;
import com.woozuda.backend.note.repository.NoteRepository;
import com.woozuda.backend.note.repository.QuestionNoteRepository;
import com.woozuda.backend.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static com.woozuda.backend.note.entity.type.Visibility.PRIVATE;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionNoteService {

    private final QuestionNoteRepository questionNoteRepository;
    private final NoteRepository noteRepository;
    private final DiaryRepository diaryRepository;
    private final QuestionRepository questionRepository;
    private final AlarmService alarmService;
    private final ImageService imageService;

    public NoteIdResponseDto saveQuestionNote(String username, QuestionNoteSaveRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }

        Question foundQuestion = questionRepository.findByTodayDate(LocalDate.parse(requestDto.getDate()));

        QuestionNote questionNote = QuestionNote.of(foundDiary,
                requestDto.getTitle(),
                LocalDate.parse(requestDto.getDate()),
                PRIVATE,
                foundQuestion,
                Feeling.fromName(requestDto.getFeeling()),
                Weather.fromName(requestDto.getWeather()),
                Season.fromName(requestDto.getSeason())
        );
        QuestionNote savedQuestionNote= noteRepository.save(questionNote);

        NoteContent noteContent = NoteContent.of(1, requestDto.getContent());
        savedQuestionNote.addContent(noteContent);

//        foundDiary.addNote(savedQuestionNote.getDate());

        // 이번에 저장한 질문일기가 그 주의 3번째 일기라면(자유일기 + 질문일기 기준), 알람을 발생합니다.
        alarmService.threePostAlarm(username, requestDto.getDate());

        // 이미지 테이블 반영 (질문 일기 생성 후)
        imageService.afterCreate(ImageType.NOTE, savedQuestionNote.getId(), requestDto.getContent());

        return NoteIdResponseDto.of(savedQuestionNote.getId());
    }

    //TODO 조회하는 노트가 로그인한 사용자의 노트인지 확인
    public NoteResponseDto getQuestionNote(String username, Long noteId) {
        NoteResponseDto responseDto = noteRepository.searchQuestionNote(noteId);
        return responseDto.convertEnum();
    }

    public NoteIdResponseDto updateQuestionNote(String username, Long noteId, QuestionNoteModifyRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }

        QuestionNote foundNote = questionNoteRepository.findById(noteId)
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

        // 이미지 테이블 반영 (질문 일기 변경 후)
        imageService.afterUpdate(ImageType.NOTE, noteId, requestDto.getContent());

        return NoteIdResponseDto.of(foundNote.getId());
    }
}
