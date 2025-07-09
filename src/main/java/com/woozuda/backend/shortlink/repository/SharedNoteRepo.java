package com.woozuda.backend.shortlink.repository;

import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.shortlink.dto.note.SharedNoteDto;

import java.util.List;

public interface SharedNoteRepo {
    List<SharedNoteDto> searchSharedQuestionNote(String username);

    List<SharedNoteDto> searchSharedCommonNote(String username);

    List<SharedNoteDto> searchSharedRetrospectiveNote(String username);

    List<String> searchNoteContent(Note note);

    Long noteCountToMakeReport(String username, String date);
}
