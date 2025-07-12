package com.woozuda.backend.event.event;

import com.woozuda.backend.note.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NoteDeletedEvent {
    private Note note;
}
