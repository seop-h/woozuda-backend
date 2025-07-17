package com.woozuda.backend.diary.service;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.diary.dto.request.DiarySaveRequestDto;
import com.woozuda.backend.diary.dto.response.DiaryDetailResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryIdResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryListResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryNameListResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryNameResponseDto;
import com.woozuda.backend.diary.dto.response.SingleDiaryResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.NoteEntryResponseDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.repository.NoteRepository;
import com.woozuda.backend.tag.entity.Tag;
import com.woozuda.backend.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public DiaryListResponseDto getDiaryListNoAuth(Long id) {
        List<SingleDiaryResponseDto> diaryList = diaryRepository.searchDiarySummaryList(id);
        return new DiaryListResponseDto(diaryList);

//        List<SingleDiaryResponseDto> result = new ArrayList<>();
//        for (Diary aDiary : diaryList) {
//            result.add(SingleDiaryResponseDto.from(aDiary));
//        }
//
//        return new DiaryListResponseDto(result);
    }

    /*@Cacheable(
            cacheNames = "getDairyList", //캐시 이름 설정
            key = " 'diaryList:username:' + #username", //Redis에 저장할 key 이름 설정
            cacheManager = "redisCacheManager" //사용할 cacheManager의 Bean 이름 지정
    )*/
    @Transactional(readOnly = true)
    public DiaryListResponseDto getDairyList(String username) {
        List<SingleDiaryResponseDto> diaryList = diaryRepository.searchDiarySummaryList(username);
        return new DiaryListResponseDto(diaryList);
    }

    /*
    TODO 프론트엔드에서 일기 조회 시 다이어리 ID도 추가해달라는 요청
        그래서 NoteResponseDto 스펙을 변경했지만, 이것이 다이어리 단건 조회 시의 응답 스펙에도 영향을 미침
        일기 전체/단건 조회, 다이어리 단건 조회 모두 똑같은 NoteResponseDto 를 사용하니까.
        다이어리 단건 조회 시, 어차피 diarySummary에 ID 값이 있으니 ...NoteDtoList 에 diaryId가 포함될 필요가 없음.
        -> 각 api마다 별도의 dto를 두는 것이 가장 좋은 방법
     */
    @Transactional(readOnly = true)
    public DiaryDetailResponseDto getOneDiary(String username, Long diaryId, Pageable pageable) {
        SingleDiaryResponseDto diarySummary = diaryRepository.searchSingleDiarySummary(username, diaryId);

        List<NoteResponseDto> commonNoteDtoList = noteRepository.searchCommonNoteList(List.of(diaryId), new NoteCondRequestDto());
        List<NoteResponseDto> questionNoteDtoList = noteRepository.searchQuestionNoteList(List.of(diaryId), new NoteCondRequestDto());
        List<NoteResponseDto> retrospectiveNoteDtoList = noteRepository.searchRetrospectiveNoteList(List.of(diaryId), new NoteCondRequestDto());

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

        Page<NoteEntryResponseDto> page;
        if (start > end) {
            page = new PageImpl<>(Collections.emptyList(), pageable, allContent.size());
        } else {
            page = new PageImpl<>(allContent.subList(start, end), pageable, allContent.size());
        }

        return DiaryDetailResponseDto.of(diarySummary, page);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponseDto getOneDiaryNoAuth(Long diaryId, Pageable pageable) {
        SingleDiaryResponseDto diarySummary = diaryRepository.searchSingleDiarySummary(diaryId);

        List<NoteResponseDto> commonNoteDtoList = noteRepository.searchCommonNoteList(List.of(diaryId), new NoteCondRequestDto());
        List<NoteResponseDto> questionNoteDtoList = noteRepository.searchQuestionNoteList(List.of(diaryId), new NoteCondRequestDto());
        List<NoteResponseDto> retrospectiveNoteDtoList = noteRepository.searchRetrospectiveNoteList(List.of(diaryId), new NoteCondRequestDto());

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

        Page<NoteEntryResponseDto> page;
        if (start > end) {
            page = new PageImpl<>(Collections.emptyList(), pageable, allContent.size());
        } else {
            page = new PageImpl<>(allContent.subList(start, end), pageable, allContent.size());
        }

        return DiaryDetailResponseDto.of(diarySummary, page);
    }

    public DiaryIdResponseDto saveDiary(String username, DiarySaveRequestDto requestDto) {
        UserEntity foundUser = userRepository.findByUsername(username);
        Diary diary = Diary.of(foundUser, requestDto.getImgUrl(), requestDto.getTitle());

        List<String> tags = requestDto.getSubject();
        for (String tagName : tags) {
            Tag foundTag = tagRepository.findByName(tagName);
            if (foundTag == null) {
                foundTag = tagRepository.save(Tag.of(tagName));
            }
            diary.addTag(foundTag);
        }

        Diary savedDiary = diaryRepository.save(diary);

        // 이미지 테이블 변경(다이어리 생성 시)
        imageService.afterCreate(ImageType.DIARY, savedDiary.getId(), requestDto.getImgUrl());

        return new DiaryIdResponseDto(savedDiary.getId());
    }

    public DiaryIdResponseDto updateDiary(String username, Long diaryId, DiarySaveRequestDto requestDto) {
        UserEntity foundUser = userRepository.findByUsername(username);

        List<String> tagNames = requestDto.getSubject();
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag foundTag = tagRepository.findByName(tagName);
            if (foundTag == null) {
                foundTag = tagRepository.save(Tag.of(tagName));
            }
            tags.add(foundTag);
        }

        Optional<Diary> foundDiary = diaryRepository.findByIdAndUser(diaryId, foundUser);
        if (foundDiary.isPresent()) {
            Diary diary = foundDiary.get();
            diary.change(requestDto.getTitle(), tags, requestDto.getImgUrl());
        }

        // 이미지 테이블 반영 (다이어리 변경 시)
        imageService.afterUpdate(ImageType.DIARY, diaryId, requestDto.getImgUrl());

        return new DiaryIdResponseDto(foundDiary.get().getId());
    }

    //TODO 배포 환경에서는 Diary 를 지우면 관련 DiaryTag 도 모두 지워질 수 있도록 데이터베이스 차원에서 cascade delete 설정
    //TODO 조회 및 삭제 쿼리가 너무 많이 나감 -> 최적화하기
    public void removeDiary(String username, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("Diary Not Found"));
        if (!diary.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("This diary does not belong to the user.");
        }

        // 이미지 테이블 반영 (다이어리 삭제 시)
        imageService.afterDelete(ImageType.DIARY, diaryId);

        diaryRepository.deleteById(diaryId);
    }

    public DiaryNameListResponseDto getDiaryNames(String username) {
        return diaryRepository.searchNames(username);
    }
}
