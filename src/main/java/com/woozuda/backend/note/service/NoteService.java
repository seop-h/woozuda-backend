package com.woozuda.backend.note.service;

import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.event.event.NoteDeletedEvent;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.request.NoteIdRequestDto;
import com.woozuda.backend.note.dto.response.DateInfoResponseDto;
import com.woozuda.backend.note.dto.response.DateListResponseDto;
import com.woozuda.backend.note.dto.response.NoteCountResponseDto;
import com.woozuda.backend.note.dto.response.NoteEntryResponseDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.dto.response.NoteResponseRepoDtoInterface;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.note.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository noteRepository;
    private final DiaryRepository diaryRepository;
    private final ImageService imageService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 최신순 일기 조회
     * <p>
     * 사용자 이름이 username 인 일기 중
     * 1. 자유 일기 관련 NoteEntryResponseDto List 조회
     * 2. 오늘의 질문 일기 관련 NoteEntryResponseDto List 조회
     * 3. 회고 관련 NoteEntryResponseDto List 조회
     * <p>
     * 3개의 List 를 묶어서 하나의 Page 로 처리
     *
     * TODO 성능 향상
     * 1. 다이어리와 사용자 테이블을 조인해서 해당 사용자가 작성한 다이어리 id 리스트를 조회하는 쿼리를 먼저 날린 다음,
     * 해당 id 리스트를 기반으로 각 일기 종류를 조회 -> user 테이블 조인 x => 적용
     * 2. createdBy 같은 기본 필드를 다이어리, 일기 등에 추가하고, 로그인한 사용자의 username 을 자동으로 저장하도록 변경
     * -> user 테이블 조인 x, 1번 방법처럼 쿼리를 하나 미리 보내지 않아도 됨
     * 3. 레디스 사용
     */
    @Transactional(readOnly = true)
    public Page<NoteEntryResponseDto> getNoteList(String username, Pageable pageable, NoteCondRequestDto condition) {
        List<Long> idList = diaryRepository.searchDiaryIdList(username);

        List<NoteResponseDto> commonNoteDtoList = noteRepository.searchCommonNoteList(idList, condition);
        List<NoteResponseDto> questionNoteDtoList = noteRepository.searchQuestionNoteList(idList, condition);
        List<NoteResponseDto> retrospectiveNoteDtoList = noteRepository.searchRetrospectiveNoteList(idList, condition);

        List<NoteEntryResponseDto> allContent = Stream.of(
                        commonNoteDtoList.stream()
                                .map(noteResponseDto -> new NoteEntryResponseDto("COMMON", noteResponseDto.convertEnum())),
                        questionNoteDtoList.stream()
                                .map(noteResponseDto -> new NoteEntryResponseDto("QUESTION", noteResponseDto.convertEnum())),
                        retrospectiveNoteDtoList.stream()
                                .map(noteResponseDto -> new NoteEntryResponseDto("RETROSPECTIVE", noteResponseDto.convertEnum()))
                ).flatMap(stream -> stream)
                .sorted(Comparator.naturalOrder())
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allContent.size());

        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, allContent.size());
        } else {
            return new PageImpl<>(allContent.subList(start, end), pageable, allContent.size());
        }
    }

    //TODO weather, season, feeling이 영어로 출력되는 거 한글로 변경
    @Transactional(readOnly = true)
    public Page<NoteEntryResponseDto> getNoteListWithJoin(String username, Pageable pageable, NoteCondRequestDto condition) {
        List<NoteResponseRepoDtoInterface> repoDtoList = noteRepository.searchNoteListWithJoin(username);

        List<NoteEntryResponseDto> allContent = new ArrayList<>();
        for (NoteResponseRepoDtoInterface repoDto : repoDtoList) {
            if (!allContent.isEmpty()
                    && allContent.getLast().getNote().getId().equals(repoDto.getNoteId())) {
                allContent.getLast().getNote().addContent(repoDto.getContent());
            } else {
                allContent.add(new NoteEntryResponseDto(repoDto.getType(), NoteResponseDto.from(repoDto)));
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allContent.size());

        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, allContent.size());
        } else {
            return new PageImpl<>(allContent.subList(start, end), pageable, allContent.size());
        }
    }

    public Page<NoteEntryResponseDto> getNoteListWithUnionAll(String username, Pageable pageable, NoteCondRequestDto condition) {
        List<NoteResponseRepoDtoInterface> repoDtoList = noteRepository.searchNoteListWithUnionAll(username);

        List<NoteEntryResponseDto> allContent = new ArrayList<>();
        for (NoteResponseRepoDtoInterface repoDto : repoDtoList) {
            if (!allContent.isEmpty()
                    && allContent.getLast().getNote().getId().equals(repoDto.getNoteId())) {
                allContent.getLast().getNote().addContent(repoDto.getContent());
            } else {
                allContent.add(new NoteEntryResponseDto(repoDto.getType(), NoteResponseDto.from(repoDto)));
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allContent.size());

        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, allContent.size());
        } else {
            return new PageImpl<>(allContent.subList(start, end), pageable, allContent.size());
        }
    }

    @Transactional(readOnly = true)
    public DateListResponseDto getNoteDates(String username) {
        List<Long> idList = diaryRepository.searchDiaryIdList(username);

        List<DateInfoResponseDto> dateList = noteRepository.searchDateCounts(idList);
        return DateListResponseDto.of(dateList);
    }

    /*
    TODO notesToDelete 의 id 리스트가 requestDto.getId() 와 정확히 일치해야 함
    TODO 이것도 삭제하고자 하는 엔티티가 로그인한 사용자의 것인지 확인하는 로직이 없음
        차라리 노트 엔티티에 createdBy나 ownedBy 같은 칼럼을 추가해서 username 을 직접 들고 있게 하는 게 좋을 듯
    TODO 회고의 경우 연관된 NoteContent 를 일괄적으로 삭제하는 게 아니라 하나씩 삭제함 -> 성능 최적화
    TODO 이 서비스 로직이 끝나고, 관련된 noteContent를 삭제하기 위해 각 note의 noteContent를 조회하는 쿼리가 나감
        즉 3개의 note를 삭제하려면 3변의 'select note_content from note_content where note_content.note_id = ?' 가 나가는 것 (n+1)
        -> 성능 최적화하기
     */
    public void deleteNotes(String username, NoteIdRequestDto requestDto) {
        List<Diary> diariesToChange = diaryRepository.searchDiariesHaving(requestDto.getId());

        log.info("diaries To Change size = {}", diariesToChange.size());

        /*for (Diary diary : diariesToChange) {
            diary.updateNoteInfo(requestDto.getId());
        }*/


        //해당 노트에 써있던 이미지들 삭제
        List<Long> deleteNoteIds = requestDto.getId();

        for(Long deleteNoteId : deleteNoteIds){
            imageService.afterDelete(ImageType.NOTE, deleteNoteId);
        }

    }

    public void deleteNote(String username, Long noteId) {
        // 노트 조회 및 권한 확인
        Note note = noteRepository.findWithRelatedById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        // 해당 노트가 사용자의 것인지 확인
        if (!note.getDiary().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("This note does not belong to the user.");
        }

        // 노트 삭제
        noteRepository.delete(note);

        // 이벤트 발행
        eventPublisher.publishEvent(
                new NoteDeletedEvent(note)
        );

        // 이미지 테이블에서 관련 이미지 정보 삭제
        imageService.afterDelete(ImageType.NOTE, noteId);
    }

    public NoteCountResponseDto getNoteCount(String username, LocalDate startDate, LocalDate endDate) {
        List<Long> idList = diaryRepository.searchDiaryIdList(username);

        return noteRepository.searchNoteCount(idList, startDate, endDate);
    }
}
