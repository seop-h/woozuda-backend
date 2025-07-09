package com.woozuda.backend.note.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.DateInfoResponseDto;
import com.woozuda.backend.note.dto.response.DateListResponseDto;
import com.woozuda.backend.note.dto.response.NoteCountResponseDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.dto.response.QNoteResponseDto;
import com.woozuda.backend.note.entity.Note;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.woozuda.backend.diary.entity.QDiary.diary;
import static com.woozuda.backend.note.entity.QCommonNote.commonNote;
import static com.woozuda.backend.note.entity.QNote.note;
import static com.woozuda.backend.note.entity.QNoteContent.noteContent;
import static com.woozuda.backend.note.entity.QQuestionNote.questionNote;
import static com.woozuda.backend.note.entity.QRetrospectiveNote.retrospectiveNote;
import static com.woozuda.backend.question.entity.QQuestion.question;
import static java.lang.Long.sum;

/**
 * TODO 성능 생각하지 않음. 기능 완성한 뒤에는 성능 향상시킬 것
 */
public class CustomNoteRepositoryImpl implements CustomNoteRepository {

    private final JPAQueryFactory query;

    public CustomNoteRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<NoteResponseDto> searchCommonNoteList(List<Long> idList, NoteCondRequestDto condition) {
        return query
                .from(commonNote)
                .leftJoin(commonNote.diary, diary)
                .leftJoin(noteContent).on(noteContent.note.id.eq(commonNote.id))
                .where(dateEq(condition.getDate()), diary.id.in(idList))
                .transform(
                        groupBy(commonNote.id).list(
                                new QNoteResponseDto(
                                        commonNote.id,
                                        diary.id,
                                        diary.title,
                                        note.title,
                                        note.date.stringValue(),
                                        commonNote.weather.stringValue(),
                                        commonNote.season.stringValue(),
                                        commonNote.feeling.stringValue(),
                                        list(
                                                noteContent.content
                                        )
                                )
                        )
                );
    }

    @Override
    public List<NoteResponseDto> searchQuestionNoteList(List<Long> idList, NoteCondRequestDto condition) {
        return query
                .from(questionNote)
                .leftJoin(questionNote.diary, diary)
                .leftJoin(noteContent).on(noteContent.note.id.eq(questionNote.id))
                .where(dateEq(condition.getDate()), diary.id.in(idList))
                .transform(
                        groupBy(questionNote.id).list(
                                new QNoteResponseDto(
                                        questionNote.id,
                                        diary.id,
                                        diary.title,
                                        note.title,
                                        note.date.stringValue(),
                                        questionNote.weather.stringValue(),
                                        questionNote.season.stringValue(),
                                        questionNote.feeling.stringValue(),
                                        questionNote.question.content,
                                        list(
                                                noteContent.content
                                        )
                                )
                        )
                );
    }

    @Override
    public List<NoteResponseDto> searchRetrospectiveNoteList(List<Long> idList, NoteCondRequestDto condition) {
        return query
                .from(retrospectiveNote)
                .leftJoin(retrospectiveNote.diary, diary)
                .leftJoin(noteContent)
                .on(noteContent.note.id.eq(retrospectiveNote.id))
                .where(dateEq(condition.getDate()), diary.id.in(idList))
                .orderBy(retrospectiveNote.id.asc(), noteContent.noteOrder.asc())
                .transform(
                        groupBy(retrospectiveNote.id).list(
                                new QNoteResponseDto(
                                        retrospectiveNote.id,
                                        diary.id,
                                        diary.title,
                                        note.title,
                                        note.date.stringValue(),
                                        retrospectiveNote.type.stringValue(),
                                        list(
                                                noteContent.content
                                        )
                                )
                        )
                );
    }

    @Override
    public NoteResponseDto searchCommonNote(Long noteId) {
        return query
                .from(commonNote)
                .leftJoin(commonNote.diary, diary)
                .leftJoin(noteContent)
                .on(noteContent.note.id.eq(commonNote.id))
                .where(commonNote.id.eq(noteId))
                .transform(
                        groupBy(commonNote.id).list(
                                new QNoteResponseDto(
                                        commonNote.id,
                                        diary.id,
                                        diary.title,
                                        commonNote.title,
                                        commonNote.date.stringValue(),
                                        commonNote.weather.stringValue(),
                                        commonNote.season.stringValue(),
                                        commonNote.feeling.stringValue(),
                                        list(noteContent.content)
                                )
                        )
                )
                .getFirst();
    }

    @Override
    public NoteResponseDto searchQuestionNote(Long noteId) {
        return query
                .from(questionNote)
                .leftJoin(questionNote.question, question)
                .leftJoin(questionNote.diary, diary)
                .leftJoin(noteContent)
                .on(noteContent.note.id.eq(questionNote.id))
                .where(questionNote.id.eq(noteId))
                .transform(
                        groupBy(questionNote.id).list(
                                new QNoteResponseDto(
                                        questionNote.id,
                                        diary.id,
                                        diary.title,
                                        questionNote.title,
                                        questionNote.date.stringValue(),
                                        questionNote.weather.stringValue(),
                                        questionNote.season.stringValue(),
                                        questionNote.feeling.stringValue(),
                                        question.content,
                                        list(noteContent.content)
                                )
                        )
                )
                .getFirst();
    }

    @Override
    public NoteResponseDto searchRetrospectiveNote(Long noteId) {
        return query
                .from(retrospectiveNote)
                .leftJoin(retrospectiveNote.diary, diary)
                .leftJoin(noteContent)
                .on(noteContent.note.id.eq(retrospectiveNote.id))
                .where(retrospectiveNote.id.eq(noteId))
                .transform(
                        groupBy(retrospectiveNote.id).list(
                                new QNoteResponseDto(
                                        retrospectiveNote.id,
                                        diary.id,
                                        diary.title,
                                        retrospectiveNote.title,
                                        retrospectiveNote.date.stringValue(),
                                        retrospectiveNote.type.stringValue(),
                                        list(noteContent.content)
                                )
                        )
                )
                .getFirst();
    }

    @Override
    public List<DateInfoResponseDto> searchDateCounts(List<Long> idList) {
        return query
                .select(Projections.constructor(DateInfoResponseDto.class,
                        note.date.stringValue(),
                        note.count().intValue()
                ))
                .from(note)
                .where(note.diary.id.in(idList))
                .groupBy(note.date)
                .fetch();
    }

    //TODO 쿼리로 COMMON + QUESTION, RETROSPECTIVE 카운트 한번에 가져오기
    @Override
    public NoteCountResponseDto searchNoteCount(List<Long> diaryIdList, LocalDate startDate, LocalDate endDate) {
        List<Tuple> result = query
                .select(
                        note.dtype,
                        note.count()
                )
                .from(note)
                .where(
                        note.diary.id.in(diaryIdList),
                        note.date.between(startDate, endDate)
                )
                .groupBy(note.dtype)
                .fetch();

        int nonRetroCount = 0;
        int retroCount = 0;

        for (Tuple tuple : result) {
            if (tuple.get(note.dtype).equals("COMMON") || tuple.get(note.dtype).equals("QUESTION")) {
                nonRetroCount += tuple.get(note.count());
            } else {
                retroCount += tuple.get(note.count());
            }
        }

        return new NoteCountResponseDto(nonRetroCount, retroCount);
    }

    private static BooleanExpression dateEq(LocalDate date) {
        return date == null ? null : note.date.eq(date);
    }
}
