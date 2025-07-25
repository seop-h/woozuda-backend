package com.woozuda.backend.note.repository;

import com.woozuda.backend.forai.repository.CustomNoteRepoForAi;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.NoteResponseRepoDtoInterface;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.shortlink.repository.SharedNoteRepo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>, CustomNoteRepository, CustomNoteRepoForAi, SharedNoteRepo, ComplicatedNoteQuery {

    @Query("select n.date from Note n where n.diary.id in :idList")
    List<LocalDate> findDateByDiaryIds(@Param("idList") List<Long> idList);

    @Override
    @EntityGraph(attributePaths = {"noteContents"})
    List<Note> findAllById(Iterable<Long> longs);

    @EntityGraph(attributePaths = {"diary", "diary.user", "noteContents"})
    Optional<Note> findWithRelatedById(Long noteId);

    @Query(value = """            
            select n.note_id        as noteId,
                   n.dtype          as type,
                   d.diary_id       as diaryId,
                   d.title          as diaryTitle,
                   n.title          as noteTitle,
                   n.date           as date,
                   case
                       when (n.dtype = 'COMMON')
                           then cn.weather
                       when (n.dtype = 'QUESTION')
                           then qn.weather
                       end          as weather,
                   case
                       when (n.dtype = 'COMMON')
                           then cn.season
                       when (n.dtype = 'QUESTION')
                           then qn.season
                       end          as season,
                   case
                       when (n.dtype = 'COMMON')
                           then cn.feeling
                       when (n.dtype = 'QUESTION')
                           then qn.feeling
                       end          as feeling,
                   q.content        as question,
                   rn.type          as framework,
                   nc.content       as content,
                   nc.note_order    as contentOrder
            from diary d
                     join note n
                        on d.diary_id = n.diary_id
                     left join common_note cn
                        on n.note_id = cn.note_id
                            and n.dtype = 'COMMON'
                     left join question_note qn
                        on n.note_id = qn.note_id
                            and n.dtype = 'QUESTION'
                     left join retrospective_note rn
                        on n.note_id = rn.note_id
                            and n.dtype = 'RETROSPECTIVE'
                     left join question q
                        on q.question_id = qn.question_id
                     left join note_content nc
                        on n.note_id = nc.note_id
            where d.diary_id in (select d_sub.diary_id
                                  from diary d_sub
                                           join
                                       woozuda_user u_sub
                                       on d_sub.user_id = u_sub.user_id
                                  where u_sub.username = :username)
            order by n.date desc,
                     field(n.dtype, 'COMMON', 'QUESTION', 'RETROSPECTIVE'),
                     n.note_id,
                     nc.note_order;
            """,
            nativeQuery = true)
    List<NoteResponseRepoDtoInterface> searchNoteListWithJoin(@Param("username") String username);

    @Query(value = """
            (select d.diary_id    as diaryId,
                    d.title       as diaryTitle,
                    n.note_id     as noteId,
                    n.title       as noteTitle,
                    n.date        as date,
                    nc.content    as content,
                    n.dtype       as type,
                    cn.feeling    as feeling,
                    cn.weather    as weather,
                    cn.season     as season,
                    null          as question,
                    null          as framework,
                    nc.note_order as contentOrder
             from diary d
                      join note n
                           on d.diary_id = n.diary_id
                               and n.dtype = 'COMMON'
                      join common_note cn
                           on n.note_id = cn.note_id
                      left join note_content nc
                                on n.note_id = nc.note_id
             where d.diary_id in (select d_sub.diary_id
                                  from diary d_sub
                                           join
                                       woozuda_user u_sub
                                       on d_sub.user_id = u_sub.user_id
                                  where u_sub.username = :username))
            
            union all
            
            (select d.diary_id    as diaryId,
                    d.title       as diaryTitle,
                    n.note_id     as noteId,
                    n.title       as noteTitle,
                    n.date        as date,
                    nc.content    as content,
                    n.dtype       as type,
                    qn.feeling    as feeling,
                    qn.weather    as weather,
                    qn.season     as season,
                    q.content     as question,
                    null          as framwork,
                    nc.note_order as contentOrder
             from diary d
                      join note n
                           on d.diary_id = n.diary_id
                               and n.dtype = 'QUESTION'
                      join question_note qn
                           on n.note_id = qn.note_id
                      join question q
                           on qn.question_id = q.question_id
                      left join note_content nc
                                on n.note_id = nc.note_id
             where d.diary_id in (select d_sub.diary_id
                                  from diary d_sub
                                           join
                                       woozuda_user u_sub
                                       on d_sub.user_id = u_sub.user_id
                                  where u_sub.username = :username))
            
            
            union all
            
            (select d.diary_id    as diaryId,
                    d.title       as diaryTitle,
                    n.note_id     as noteId,
                    n.title       as noteTitle,
                    n.date        as date,
                    nc.content    as content,
                    n.dtype       as type,
                    null          as feeling,
                    null          as weather,
                    null          as season,
                    null          as question,
                    rn.type       as framework,
                    nc.note_order as contentOrder
             from diary d
                      join note n
                           on d.diary_id = n.diary_id
                               and n.dtype = 'RETROSPECTIVE'
                      join retrospective_note rn
                           on n.note_id = rn.note_id
                      left join note_content nc
                                on n.note_id = nc.note_id
             where d.diary_id in (select d_sub.diary_id
                                  from diary d_sub
                                           join
                                       woozuda_user u_sub
                                       on d_sub.user_id = u_sub.user_id
                                  where u_sub.username = :username))
            order by date desc,
                     field(type, 'COMMON', 'QUESTION', 'RETROSPECTIVE'),
                     noteId,
                     contentOrder;
            """,
            nativeQuery = true)
    List<NoteResponseRepoDtoInterface> searchNoteListWithUnionAll(@Param("username") String username);

}