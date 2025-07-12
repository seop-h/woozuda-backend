package com.woozuda.backend.event.handler;

import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.event.event.NoteCreatedEvent;
import com.woozuda.backend.event.event.NoteDeletedEvent;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class DiaryAggregatedEventHandler {

    private final DiaryRepository diaryRepository;

    @EventListener
    public void handleNoteCreated(NoteCreatedEvent event) {
        Diary diary = diaryRepository.findById(event.getDiaryId())
                .orElseThrow(() -> new EntityNotFoundException("diary not found"));
        diary.updateAfterAddNote(event.getNoteDate());
    }

    @EventListener
    public void handleNoteDeleted(NoteDeletedEvent event) {
        Diary diary = diaryRepository.findById(event.getNote().getDiary().getId())
                .orElseThrow(() -> new EntityNotFoundException("diary not found"));
        diary.updateAfterDeleteNote(event.getNote());
    }

}
