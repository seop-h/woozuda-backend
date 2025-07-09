package com.woozuda.backend.diary.controller;

import com.woozuda.backend.account.dto.CustomUser;
import com.woozuda.backend.diary.dto.request.DiarySaveRequestDto;
import com.woozuda.backend.diary.dto.response.DiaryDetailResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryIdResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryListResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryNameListResponseDto;
import com.woozuda.backend.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/no-auth/{id}")
    public ResponseEntity<DiaryListResponseDto> getDiaryListNoAuth(
            @PathVariable("id") Long id
    ) {
        DiaryListResponseDto responseDto = diaryService.getDiaryListNoAuth(id);
        return ResponseEntity.ok(responseDto);
    }

    /*
    TODO 모든 컨트롤러에 @AuthenticationPrincipal CustomUser user, String username = user.getUsername()이 중복됨
         파라미터 레벨에서 바로 username 을 받을 수 있도록 커스텀 애너테이션을 추가하는 등 중복 줄이기
         ex) @AuthenticationUsername String username
     */
    @GetMapping
    public ResponseEntity<DiaryListResponseDto> getDiaryList(
            @AuthenticationPrincipal CustomUser user
    ) {
        String username = user.getUsername();
        DiaryListResponseDto responseDto = diaryService.getDairyList(username);
        return ResponseEntity.ok(responseDto);
    }


    @PostMapping
    public ResponseEntity<DiaryIdResponseDto> createDiary(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody @Valid DiarySaveRequestDto requestDto
    ) {
        String username = user.getUsername();
        DiaryIdResponseDto responseDto = diaryService.saveDiary(username, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * TODO 해당 api를 호출하면 스프링부트가 스프링 데이터의 PageModel 을 사용하라고 경고를 줌
     * https://docs.spring.io/spring-data/commons/reference/repositories/core-extensions.html#core.web.pageables
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponseDto> getDiaryDetail(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable("diaryId") Long diaryId,
            @PageableDefault Pageable pageable
    ) {
        String username = user.getUsername();
        DiaryDetailResponseDto responseDto = diaryService.getOneDiary(username, diaryId, pageable);
        return ResponseEntity.ok(responseDto);
    }


    @PatchMapping("/{diaryId}")
    public ResponseEntity<DiaryIdResponseDto> modifyDiary(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable("diaryId") Long diaryId,
            @RequestBody @Valid DiarySaveRequestDto requestDto
    ) {
        String username = user.getUsername();
        DiaryIdResponseDto responseDto = diaryService.updateDiary(username, diaryId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable("diaryId") Long diaryId
    ) {
        String username = user.getUsername();
        diaryService.removeDiary(username, diaryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/name")
    public ResponseEntity<DiaryNameListResponseDto> getDiaryNames(
            @AuthenticationPrincipal CustomUser user
    ) {
        String username = user.getUsername();
        DiaryNameListResponseDto responseDto = diaryService.getDiaryNames(username);
        return ResponseEntity.ok(responseDto);
    }

}
