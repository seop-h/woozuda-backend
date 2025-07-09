package com.woozuda.backend.note.service;

import com.woozuda.backend.diary.dto.response.NoteIdResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.note.dto.request.RetrospectiveNoteModifyRequestDto;
import com.woozuda.backend.note.dto.request.RetrospectiveNoteSaveRequestDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.entity.NoteContent;
import com.woozuda.backend.note.entity.QuestionNote;
import com.woozuda.backend.note.entity.RetrospectiveNote;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Framework;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Weather;
import com.woozuda.backend.note.repository.NoteRepository;
import com.woozuda.backend.note.repository.RetrospectiveNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static com.woozuda.backend.note.entity.type.Visibility.PRIVATE;

@Service
@Transactional
@RequiredArgsConstructor
public class RetrospectiveNoteService {

    private final RetrospectiveNoteRepository retrospectiveNoteRepository;
    private final NoteRepository noteRepository;
    private final DiaryRepository diaryRepository;

    public NoteIdResponseDto saveRetrospectiveNote(String username, RetrospectiveNoteSaveRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }

        RetrospectiveNote retrospectiveNote = RetrospectiveNote.of(foundDiary,
                requestDto.getTitle(),
                LocalDate.parse(requestDto.getDate()),
                PRIVATE,
                Framework.valueOf(requestDto.getType())
        );

        RetrospectiveNote savedRetrospectiveNote = noteRepository.save(retrospectiveNote);

        List<String> content = requestDto.getContent();
        for (int i = 0; i < content.size(); i++) {
            NoteContent noteContent = NoteContent.of(i + 1, content.get(i));
            savedRetrospectiveNote.addContent(noteContent);
        }

//        foundDiary.addNote(savedRetrospectiveNote.getDate());

        return NoteIdResponseDto.of(savedRetrospectiveNote.getId());
    }

    //TODO 조회하는 노트가 로그인한 사용자의 노트인지 확인
    public NoteResponseDto getRetrospectiveNote(String username, Long noteId) {
        NoteResponseDto responseDto = noteRepository.searchRetrospectiveNote(noteId);
        return responseDto.convertEnum();
    }

    public NoteIdResponseDto updateRetrospectiveNote(String username, Long noteId, RetrospectiveNoteModifyRequestDto requestDto) {
        Diary foundDiary = diaryRepository.searchDiary(requestDto.getDiaryId(), username);
        if (foundDiary == null) {
            throw new IllegalArgumentException("Diary not found.");
        }

        RetrospectiveNote foundNote = retrospectiveNoteRepository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException("Note not found"));
        foundNote.update(
                foundDiary,
                requestDto.getTitle(),
                LocalDate.parse(requestDto.getDate()),
                Framework.valueOf(requestDto.getType()),
                requestDto.getContent()
        );

        return NoteIdResponseDto.of(foundNote.getId());
    }
}
