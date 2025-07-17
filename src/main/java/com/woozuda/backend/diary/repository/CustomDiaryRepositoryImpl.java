package com.woozuda.backend.diary.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woozuda.backend.diary.dto.response.DiaryNameListResponseDto;
import com.woozuda.backend.diary.dto.response.DiaryNameResponseDto;
import com.woozuda.backend.diary.dto.response.SingleDiaryResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.note.entity.QNote;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.jpa.JPAExpressions.select;
import static com.woozuda.backend.account.entity.QUserEntity.userEntity;
import static com.woozuda.backend.diary.entity.QDiary.diary;
import static com.woozuda.backend.diary.entity.QDiaryTag.diaryTag;
import static com.woozuda.backend.note.entity.QNote.note;
import static com.woozuda.backend.tag.entity.QTag.tag;

public class CustomDiaryRepositoryImpl implements CustomDiaryRepository {

    private final JPAQueryFactory query;

    public CustomDiaryRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<SingleDiaryResponseDto> searchDiarySummaryList(Long id) {
        return query
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(userEntity.id.eq(id))
                .orderBy(diary.createdAt.desc())
                .transform(
                        groupBy(diary.id).list(
                                Projections.constructor(SingleDiaryResponseDto.class,
                                        diary.id,
                                        diary.title,
                                        list(tag.name),
                                        diary.image,
                                        diary.startDate,
                                        diary.endDate,
                                        diary.noteCount
                                )
                        )
                );
        // 1. 다이어리 기본 정보와 노트 집계 정보 조회
        /*
        List<Diary> diaryList = query
                .selectFrom(diary)
                .join(diary.user, userEntity)
                .where(userEntity.id.eq(id))
                .fetch();
                */

        /*
        List<Diary> diaryList = query
                .selectFrom(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag).fetchJoin()
                .leftJoin(diaryTag.tag, tag).fetchJoin()
                .where(userEntity.id.eq(id))
                .orderBy(diary.createdAt.desc())
                .fetch();


        return diaryList;
        */

        /*
        List<SingleDiaryResponseDto> result = new ArrayList<>();
        for (Diary aDiary : diaryList) {
            result.add(SingleDiaryResponseDto.from(aDiary));
        }

        return result;
        */

        /*
        List<Tuple> diaryResults = query
                .select(
                        diary.id,
                        diary.title,
                        diary.image,
                        note.date.min(),
                        note.date.max(),
                        note.count().intValue()
                )
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.noteList, note)
                .where(userEntity.id.eq(id))
                .groupBy(diary.id, diary.title, diary.image)
                .orderBy(diary.createdAt.desc())
                .fetch();

        // 2. 다이어리별 태그 정보 조회
        Map<Long, List<String>> tagMap = query
                .select(diary.id, tag.name)
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(userEntity.id.eq(id))
                .fetch()
                .stream()
                .collect(HashMap::new, 
                        (map, tuple) -> {
                            Long diaryId = tuple.get(diary.id);
                            String tagName = tuple.get(tag.name);
                            if (tagName != null) {
                                map.computeIfAbsent(diaryId, k -> new ArrayList<>()).add(tagName);
                            }
                        }, 
                        (map1, map2) -> {
                            map2.forEach((key, value) -> map1.merge(key, value, (v1, v2) -> { v1.addAll(v2); return v1; }));
                        });

        // 3. 결과 조합
        return diaryResults.stream()
                .map(tuple -> new SingleDiaryResponseDto(
                        tuple.get(diary.id),
                        tuple.get(diary.title),
                        tagMap.getOrDefault(tuple.get(diary.id), new ArrayList<>()),
                        tuple.get(diary.image),
                        tuple.get(note.date.min()),
                        tuple.get(note.date.max()),
                        tuple.get(note.count().intValue())
                ))
                .toList();
        */

        /*
        List<Tuple> results = query
                .select(
                        diary.id,
                        diary.title,
                        diary.image,
                        diary.startDate,
                        diary.endDate,
                        diary.noteCount,
                        tag.name
                )
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(userEntity.id.eq(id))
                .orderBy(diary.createdAt.desc())
                .fetch();

        // 결과를 DTO로 매핑
        Map<Long, SingleDiaryResponseDto> diaryMap = new HashMap<>();

        results.forEach(tuple -> {
            Long diaryId = tuple.get(diary.id);
            SingleDiaryResponseDto dto = diaryMap.getOrDefault(diaryId, new SingleDiaryResponseDto(
                    diaryId,
                    tuple.get(diary.title),
                    new ArrayList<>(),
                    tuple.get(diary.image),
                    tuple.get(diary.startDate),
                    tuple.get(diary.endDate),
                    tuple.get(diary.noteCount)
            ));

            String tagName = tuple.get(tag.name);
            if (tagName != null) {
                dto.getSubject().add(tagName);
            }

            diaryMap.put(diaryId, dto);
        });

        return new ArrayList<>(diaryMap.values());
        */
    }

    @Override
    public List<SingleDiaryResponseDto> searchDiarySummaryList(String username) {
        return query
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .leftJoin(diary.noteList, note)
                .where(userEntity.username.eq(username))
                .transform(
                        groupBy(diary.id).list(
                                Projections.constructor(SingleDiaryResponseDto.class,
                                        diary.id,
                                        diary.title,
                                        list(
                                                tag.name
                                        ),
                                        diary.image,
                                        note.date.min(),
                                        note.date.max(),
                                        note.count().intValue()
                                )
                        )
                );

        /*
        // 쿼리 작성
        List<Tuple> results = query
                .select(
                        diary.id,
                        diary.title,
                        diary.image,
                        diary.startDate,
                        diary.endDate,
                        diary.noteCount,
                        tag.name
                )
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(userEntity.username.eq(username))
                .orderBy(diary.createdAt.desc())
                .fetch();

        // 결과를 DTO로 매핑
        Map<Long, SingleDiaryResponseDto> diaryMap = new HashMap<>();

        results.forEach(tuple -> {
            Long diaryId = tuple.get(diary.id);
            SingleDiaryResponseDto dto = diaryMap.getOrDefault(diaryId, new SingleDiaryResponseDto(
                    diaryId,
                    tuple.get(diary.title),
                    new ArrayList<>(),
                    tuple.get(diary.image),
                    tuple.get(diary.startDate),
                    tuple.get(diary.endDate),
                    tuple.get(diary.noteCount)
            ));

            String tagName = tuple.get(tag.name);
            if (tagName != null) {
                dto.getSubject().add(tagName);
            }

            diaryMap.put(diaryId, dto);
        });

        return new ArrayList<>(diaryMap.values());
        */
    }

    @Override
    public SingleDiaryResponseDto searchSingleDiarySummary(String username, Long diaryId) {
        return query
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(diary.id.eq(diaryId), userEntity.username.eq(username))
                .transform(
                        groupBy(diary.id).list(
                                Projections.constructor(SingleDiaryResponseDto.class,
                                        diary.id,
                                        diary.title,
                                        list(
                                                tag.name
                                        ),
                                        diary.image,
                                        diary.startDate,
                                        diary.endDate,
                                        diary.noteCount
                                )
                        )
                )
                .getFirst();
    }

    @Override
    public SingleDiaryResponseDto searchSingleDiarySummary(Long diaryId) {
        return query
                .from(diary)
                .join(diary.user, userEntity)
                .leftJoin(diary.tagList, diaryTag)
                .leftJoin(diaryTag.tag, tag)
                .where(diary.id.eq(diaryId))
                .transform(
                        groupBy(diary.id).list(
                                Projections.constructor(SingleDiaryResponseDto.class,
                                        diary.id,
                                        diary.title,
                                        list(
                                                tag.name
                                        ),
                                        diary.image,
                                        diary.startDate,
                                        diary.endDate,
                                        diary.noteCount
                                )
                        )
                )
                .getFirst();
    }

    @Override
    public List<Long> searchDiaryIdList(String username) {
        return query
                .select(diary.id)
                .from(diary)
                .join(diary.user, userEntity)
                .where(userEntity.username.eq(username))
                .fetch();
    }

    @Override
    public Diary searchDiary(Long diaryId, String username) {
        return query
                .selectFrom(diary)
                .where(diary.user.username.eq(username), diary.id.eq(diaryId))
                .fetchFirst();
    }

    @Override
    public DiaryNameListResponseDto searchNames(String username) {
        List<DiaryNameResponseDto> nameList = query
                .select(Projections.constructor(DiaryNameResponseDto.class,
                        diary.id,
                        diary.title
                ))
                .from(diary)
                .leftJoin(diary.user, userEntity)
                .where(userEntity.username.eq(username))
                .fetch();

        return new DiaryNameListResponseDto(nameList);
    }

    @Override
    public List<Diary> searchDiariesHaving(List<Long> noteIdList) {
        QNote noteSub = new QNote("noteSub");

        return query
                .selectFrom(diary)
                .leftJoin(diary.noteList, note)
                .leftJoin(note.noteContents)
                .where(
                        diary.id.in(
                                select(noteSub.diary.id)
                                        .from(noteSub)
                                        .where(noteSub.id.in(noteIdList))
                        )
                )
                .fetch();
    }
}
