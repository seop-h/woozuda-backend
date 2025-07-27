package com.woozuda.backend.note.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woozuda.backend.account.entity.QUserEntity;
import com.woozuda.backend.diary.entity.QDiary;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.NoteEntryResponseDto;
import com.woozuda.backend.note.dto.response.QNoteEntryResponseDto;
import com.woozuda.backend.note.dto.response.QNoteResponseDto;
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

public class ComplicatedNoteQueryImpl implements ComplicatedNoteQuery {

    public static final String COMMON = "COMMON";
    public static final String QUESTION = "QUESTION";
    public static final String RETROSPECTIVE = "RETROSPECTIVE";

    private final JPAQueryFactory query;

    public ComplicatedNoteQueryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<NoteEntryResponseDto> searchNoteListWithJoin(String username, NoteCondRequestDto condition) {
        QDiary diarySub = new QDiary("diarySub");
        QUserEntity userSub = new QUserEntity("userSub");

        return query
                .from(diary)
                .leftJoin(note).on(diary.id.eq(note.diary.id))
                .leftJoin(commonNote).on(note.id.eq(commonNote.id))
                .leftJoin(questionNote).on(note.id.eq(questionNote.id))
                .leftJoin(retrospectiveNote).on(note.id.eq(retrospectiveNote.id))
                .leftJoin(question).on(question.id.eq(questionNote.question.id))
                .leftJoin(noteContent).on(note.id.eq(noteContent.note.id))
                .where(dateEq(condition.getDate()),
                        note.diary.id.in(JPAExpressions
                                .select(diarySub.id)
                                .from(diarySub)
                                .join(userSub).on(diarySub.user.id.eq(userSub.id))
                                .where(userSub.username.eq(username))
                        ))
                .orderBy(note.date.desc(),
                        Expressions.numberTemplate(
                                Integer.class,
                                "FIELD({0}, 'COMMON','QUESTION','RETROSPECTIVE')",
                                note.dtype
                        ).asc(),
                        note.id.asc(),
                        noteContent.noteOrder.asc())
                .transform(
                        groupBy(note.id).list(
                                new QNoteEntryResponseDto(
                                        note.dtype,
                                        new QNoteResponseDto(
                                                note.id,
                                                diary.id,
                                                diary.title,
                                                note.title,
                                                note.date.stringValue(),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.weather.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.weather.stringValue())
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.season.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.season.stringValue())
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.feeling.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.feeling.stringValue())
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.question.content)
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(RETROSPECTIVE)).then(retrospectiveNote.type.stringValue())
                                                        .otherwise((String) null),
                                                list(
                                                        noteContent.content
                                                )
                                        )
                                )
                        )
                );
    }

    @Override
    public List<NoteEntryResponseDto> searchSharedNoteList(Long userId) {
        QDiary diarySub = new QDiary("diarySub");
        QUserEntity userSub = new QUserEntity("userSub");

        return query
                .from(diary)
                .leftJoin(note).on(diary.id.eq(note.diary.id))
                .leftJoin(commonNote).on(note.id.eq(commonNote.id))
                .leftJoin(questionNote).on(note.id.eq(questionNote.id))
                .leftJoin(retrospectiveNote).on(note.id.eq(retrospectiveNote.id))
                .leftJoin(question).on(question.id.eq(questionNote.question.id))
                .leftJoin(noteContent).on(note.id.eq(noteContent.note.id))
                .where(note.diary.id.in(JPAExpressions
                        .select(diarySub.id)
                        .from(diarySub)
                        .join(userSub).on(diarySub.user.id.eq(userSub.id))
                        .where(userSub.id.eq(userId))
                ))
                .orderBy(note.date.desc(),
                        Expressions.numberTemplate(
                                Integer.class,
                                "FIELD({0}, 'COMMON','QUESTION','RETROSPECTIVE')",
                                note.dtype
                        ).asc(),
                        note.id.asc(),
                        noteContent.noteOrder.asc())
                .transform(
                        groupBy(note.id).list(
                                new QNoteEntryResponseDto(
                                        note.dtype,
                                        new QNoteResponseDto(
                                                note.id,
                                                diary.id,
                                                diary.title,
                                                note.title,
                                                note.date.stringValue(),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.weather.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.weather.stringValue())
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.season.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.season.stringValue())
                                                        .otherwise((String) null),
                                                new CaseBuilder()
                                                        .when(note.dtype.eq(COMMON)).then(commonNote.feeling.stringValue())
                                                        .when(note.dtype.eq(QUESTION)).then(questionNote.feeling.stringValue())
                                                        .otherwise((String) null),
                                                questionNote.question.content,
                                                retrospectiveNote.type.stringValue(),
                                                list(
                                                        noteContent.content
                                                )
                                        )
                                )
                        )
                );
    }

    private static BooleanExpression dateEq(LocalDate date) {
        return date == null ? null : note.date.eq(date);
    }
}
