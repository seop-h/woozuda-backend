package com.woozuda.backend.note.repository;

import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.NoteEntryResponseDto;

import java.util.List;

public interface ComplicatedNoteQuery {

    List<NoteEntryResponseDto> searchNoteListWithJoin(String username, NoteCondRequestDto condition);

    List<NoteEntryResponseDto> searchSharedNoteList(Long userId);
}
