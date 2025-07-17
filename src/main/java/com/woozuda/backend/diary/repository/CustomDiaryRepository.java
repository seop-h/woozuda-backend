package com.woozuda.backend.diary.repository;

import com.woozuda.backend.diary.dto.response.DiaryNameListResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryNameResponseDto;
import com.woozuda.backend.diary.dto.response.SingleDiaryResponseDto;
import com.woozuda.backend.diary.entity.Diary;

import java.util.List;

public interface CustomDiaryRepository {

    List<SingleDiaryResponseDto> searchDiarySummaryList(Long id);

    List<SingleDiaryResponseDto> searchDiarySummaryList(String username);

    SingleDiaryResponseDto searchSingleDiarySummary(String username, Long diaryId);

    SingleDiaryResponseDto searchSingleDiarySummary(Long diaryId);

    List<Long> searchDiaryIdList(String username);

    Diary searchDiary(Long diaryId, String username);

    DiaryNameListResponseDto searchNames(String username);

    List<Diary> searchDiariesHaving(List<Long> noteIdList);
}
