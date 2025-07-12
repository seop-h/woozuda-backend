package com.woozuda.backend.event.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NoteCreatedEvent {
    private final Long diaryId;
    private final LocalDate noteDate;
}
