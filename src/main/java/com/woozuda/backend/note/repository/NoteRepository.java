package com.woozuda.backend.note.repository;

import com.woozuda.backend.forai.repository.CustomNoteRepoForAi;
import com.woozuda.backend.note.entity.Note;
import org.springframework.data.jpa.repository.EntityGraph;
import com.woozuda.backend.note.entity.type.Visibility;
import com.woozuda.backend.shortlink.repository.SharedNoteRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>, CustomNoteRepository, CustomNoteRepoForAi, SharedNoteRepo {

    @Query("select n.date from Note n where n.diary.id in :idList")
    List<LocalDate> findDateByDiaryIds(@Param("idList") List<Long> idList);

    @Override
    @EntityGraph(attributePaths = {"noteContents"})
    List<Note> findAllById(Iterable<Long> longs);

    @EntityGraph(attributePaths = {"diary", "diary.user", "noteContents"})
    Optional<Note> findWithRelatedById(Long noteId);

}