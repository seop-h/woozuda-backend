package com.woozuda.backend.shortlink.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woozuda.backend.note.entity.CommonNote;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.note.entity.QuestionNote;
import com.woozuda.backend.note.entity.type.Visibility;
import com.woozuda.backend.shortlink.dto.note.SharedCommonNoteDto;
import com.woozuda.backend.shortlink.dto.note.SharedNoteDto;
import com.woozuda.backend.shortlink.dto.note.SharedQuestionNoteDto;
import com.woozuda.backend.shortlink.dto.note.SharedRetrospectiveNoteDto;
import jakarta.persistence.EntityManager;

import java.time.*;
import java.util.List;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.woozuda.backend.account.entity.QUserEntity.userEntity;
import static com.woozuda.backend.diary.entity.QDiary.diary;
import static com.woozuda.backend.note.entity.QCommonNote.commonNote;
import static com.woozuda.backend.note.entity.QNote.note;
import static com.woozuda.backend.note.entity.QNoteContent.noteContent;
import static com.woozuda.backend.note.entity.QQuestionNote.questionNote;
import static com.woozuda.backend.note.entity.QRetrospectiveNote.retrospectiveNote;

public class SharedNoteRepoImpl implements SharedNoteRepo{

    private final JPAQueryFactory query;

    public SharedNoteRepoImpl(EntityManager em){
        this.query = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<SharedNoteDto> searchSharedQuestionNote(String username) {
        return query
                .from(questionNote)
                .where(questionNote.visibility.eq(Visibility.PUBLIC)) // public 한 글만 필터
                .leftJoin(questionNote.diary, diary)
                .leftJoin(diary.user, userEntity)
                .where(userEntity.username.eq(username)) // username 일치한 것만 필터
                .leftJoin(noteContent).on(noteContent.note.id.eq(questionNote.id))
                .transform(
                        groupBy(questionNote.id).list(Projections.constructor(SharedQuestionNoteDto.class,
                                questionNote.id,
                                diary.title,
                                questionNote.title,
                                questionNote.date,
                                list(noteContent.content),
                                questionNote.question.content,
                                questionNote.feeling,
                                questionNote.weather,
                                questionNote.season)
                        ));
    }

    @Override
    public List<SharedNoteDto> searchSharedCommonNote(String username) {
        return query
                .from(commonNote)
                .where(commonNote.visibility.eq(Visibility.PUBLIC)) // public 한 글만 필터
                .leftJoin(commonNote.diary, diary)
                .leftJoin(diary.user, userEntity)
                .where(userEntity.username.eq(username)) // username 일치한 것만 필터
                .leftJoin(noteContent).on(noteContent.note.id.eq(commonNote.id))
                .transform(
                        groupBy(commonNote.id).list(Projections.constructor(SharedCommonNoteDto.class,
                                commonNote.id,
                                diary.title,
                                commonNote.title,
                                commonNote.date,
                                list(noteContent.content),
                                commonNote.feeling,
                                commonNote.weather,
                                commonNote.season)
                        ));
    }

    @Override
    public List<SharedNoteDto> searchSharedRetrospectiveNote(String username) {
        return query
                .from(retrospectiveNote)
                .where(retrospectiveNote.visibility.eq(Visibility.PUBLIC)) // public 한 글만 필터
                .leftJoin(retrospectiveNote.diary, diary)
                .leftJoin(diary.user, userEntity)
                .where(userEntity.username.eq(username)) // username 일치한 것만 필터
                .leftJoin(noteContent).on(noteContent.note.id.eq(retrospectiveNote.id))
                .transform(
                        groupBy(retrospectiveNote.id).list(Projections.constructor(SharedRetrospectiveNoteDto.class,
                                retrospectiveNote.id,
                                diary.title,
                                retrospectiveNote.title,
                                retrospectiveNote.date,
                                list(noteContent.content),
                                retrospectiveNote.type)
                        ));
    }

    // 특정 유저의 질문일기, 자유일기 수를 카운트 하는 쿼리 . (알람 기능에 쓰입니다)
    @Override
    public Long noteCountToMakeReport(String username, String date){

        //저장 노트의 월 ~ 일 범위 자르기 (월요일 00:00 ~ 그 다음주 월요일 00:00)
        LocalDate saveDate = LocalDate.parse(date);
        LocalDate thisWeekStart = saveDate.with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = thisWeekStart.plusDays(6);

        return query
                .select(note.count())
                .from(note)
                .leftJoin(note.diary, diary)
                .leftJoin(diary.user, userEntity)
                .where(userEntity.username.eq(username)
                        .and(note.instanceOf(CommonNote.class).or(note.instanceOf(QuestionNote.class)))
                        .and(note.date.between(thisWeekStart, thisWeekEnd)))
                .fetchOne();
    }


    @Override
    public List<String> searchNoteContent(Note note){
        return query
                .select(noteContent.content)
                .from(noteContent)
                .where(noteContent.note.eq(note))
                .orderBy(noteContent.noteOrder.asc())
                .fetch();
    }

}
