package com.woozuda.backend.note.repository;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.note.entity.NoteContent;
import com.woozuda.backend.note.entity.RetrospectiveNote;
import com.woozuda.backend.note.entity.type.Framework;
import com.woozuda.backend.note.entity.type.Visibility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static com.woozuda.backend.account.entity.AiType.PICTURE_NOVEL;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Slf4j
public class NoteDeleteNotWorkingTest {

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    TestEntityManager em;

    @Autowired
    private DiaryRepository diaryRepository;

    @Test
    void noteDeletingNotWork() {
        // Given
        UserEntity user = new UserEntity(null, "hwang", "asdf", "ROLE_USER", PICTURE_NOVEL, true, "hwang", "woozuda");
        em.persist(user);

        Diary diary = new Diary(user, "asdf", "Diary title1", LocalDate.now(), LocalDate.now(), 1);
        em.persist(diary);

        RetrospectiveNote note = RetrospectiveNote.of(diary, "Retrospective Note Title", LocalDate.now(), Visibility.PRIVATE, Framework.PMI);
        em.persist(note);

        NoteContent noteContent1 = NoteContent.of(note, 1, "Note Content1");
        NoteContent noteContent2 = NoteContent.of(note, 2, "Note Content2");
        NoteContent noteContent3 = NoteContent.of(note, 3, "Note Content3");
        em.persist(noteContent1);
        em.persist(noteContent2);
        em.persist(noteContent3);

        em.flush();
        em.clear();

        // When
        log.info("findWithRelatedById()");
        Note foundNote = noteRepository.findWithRelatedById(note.getId())
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        log.info("delete()");
        noteRepository.delete(foundNote);

        log.info("findById()");
        Diary foundDiary = diaryRepository.findById(foundNote.getDiary().getId())
                .orElseThrow(() -> new EntityNotFoundException("Diary not found"));

        log.info("updateAfterDeleteNote()");
        foundDiary.updateAfterDeleteNote(foundNote);

        log.info("flush()");
        em.flush();
        em.clear();

        // Then
        log.info("assert");
        assertThat(diaryRepository.findAll().size()).isEqualTo(1);
        assertThat(noteRepository.findById(1L)).isEmpty();

        /*
        // 노트 조회 및 권한 확인
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        // 해당 노트가 사용자의 것인지 확인
        if (!note.getDiary().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("This note does not belong to the user.");
        }

        // 노트 삭제 (JPQL을 사용하여 명시적으로 삭제)
        log.info("Deleting note with ID: {}, Type: {}", noteId, note.getDtype());
        noteRepository.delete(note);
        log.info("Note deleted successfully");

        // 이벤트 발행
         TODO 얘 때문에 삭제가 안 된 것
                이벤트를 발행하는 로직 내에서 Note를 한 번 더 영속화한다.
                그래서 위에서 delete()를 날렸더라도, 이미 영속성 컨텍스트에 엔티티가 남아있어서 삭제가 안된 것이다.
                https://yoonsys.tistory.com/15
                https://stackoverflow.com/questions/22688402/delete-not-working-with-jparepository
                https://velog.io/@jsb100800/spring-12

        eventPublisher.publishEvent(
                new NoteDeletedEvent(note.getDiary().getId(), note.getId(), note.getDate())
        );
        */
    }

    @Test
    void asdf() {
        UserEntity user = new UserEntity(null, "hwang", "asdf", "ROLE_USER", PICTURE_NOVEL, true, "hwang", "woozuda");
        em.persist(user);

        em.flush();
        em.clear();

        log.info("start");
        log.info("find user");
        UserEntity foundUser = em.find(UserEntity.class, 1L);

        log.info("delete user");
        em.remove(foundUser);

        log.info("find user again");
        UserEntity foundUser2 = em.find(UserEntity.class, 1L);

        em.flush();
        em.clear();
    }

    public String getEntityState(EntityManager realEm, Object entity) {
        if (entity == null) {
            return "NULL";
        }

        Session session = realEm.unwrap(Session.class);

        // 새로운 엔티티인지 확인
        /*Object id = session.getIdentifier(entity);
        if (id == null) {
            return "NEW";
        }*/

        // MANAGED 상태인지 확인
        if (realEm.contains(entity)) {
            SessionImplementor sessionImpl = (SessionImplementor) session;
            EntityEntry entry = sessionImpl.getPersistenceContext().getEntry(entity);

            if (entry != null && entry.getStatus() == Status.DELETED) {
                return "REMOVED";
            }
            return "MANAGED";
        }

        return "DETACHED";
    }

}
