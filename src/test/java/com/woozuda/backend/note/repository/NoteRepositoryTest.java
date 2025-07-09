package com.woozuda.backend.note.repository;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.note.dto.request.NoteCondRequestDto;
import com.woozuda.backend.note.dto.response.NoteResponseDto;
import com.woozuda.backend.note.entity.CommonNote;
import com.woozuda.backend.note.entity.NoteContent;
import com.woozuda.backend.note.entity.QuestionNote;
import com.woozuda.backend.note.entity.RetrospectiveNote;
import com.woozuda.backend.note.entity.converter.AesEncryptor;
import com.woozuda.backend.note.entity.converter.NoteContentConverter;
import com.woozuda.backend.question.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.woozuda.backend.account.entity.AiType.PICTURE_NOVEL;
import static com.woozuda.backend.note.entity.type.Feeling.ANGER;
import static com.woozuda.backend.note.entity.type.Feeling.CONTENT;
import static com.woozuda.backend.note.entity.type.Feeling.DISSATISFACTION;
import static com.woozuda.backend.note.entity.type.Feeling.JOY;
import static com.woozuda.backend.note.entity.type.Feeling.NEUTRAL;
import static com.woozuda.backend.note.entity.type.Feeling.SADNESS;
import static com.woozuda.backend.note.entity.type.Feeling.TIREDNESS;
import static com.woozuda.backend.note.entity.type.Framework.FOUR_F_S;
import static com.woozuda.backend.note.entity.type.Framework.KPT;
import static com.woozuda.backend.note.entity.type.Framework.PMI;
import static com.woozuda.backend.note.entity.type.Framework.SCS;
import static com.woozuda.backend.note.entity.type.Season.FALL;
import static com.woozuda.backend.note.entity.type.Season.WINTER;
import static com.woozuda.backend.note.entity.type.Visibility.PRIVATE;
import static com.woozuda.backend.note.entity.type.Visibility.PUBLIC;
import static com.woozuda.backend.note.entity.type.Weather.CLEAR;
import static com.woozuda.backend.note.entity.type.Weather.CLOUDY;
import static com.woozuda.backend.note.entity.type.Weather.RAIN;
import static com.woozuda.backend.note.entity.type.Weather.SNOW;
import static com.woozuda.backend.note.entity.type.Weather.SUNNY;
import static com.woozuda.backend.note.entity.type.Weather.THUNDERSTORM;
import static java.time.Month.DECEMBER;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class NoteRepositoryTest {

    @TestConfiguration
    static class NoteContentTestConfig {
        @Bean
        public AesEncryptor aesEncryptor() {
            return new AesEncryptor("test-password");
        }

        @Bean
        public NoteContentConverter noteContentConverter(AesEncryptor aesEncryptor) {
            return new NoteContentConverter(aesEncryptor);
        }
    }

    LocalDate date1 = LocalDate.of(2024, DECEMBER, 5);
    LocalDate date2 = LocalDate.of(2024, DECEMBER, 6);

    @Autowired
    NoteRepository noteRepository;
    @Autowired
    TestEntityManager em;

    UserEntity user;
    Diary diary1;
    Diary diary2;

    @BeforeEach
    void userDiaryAndDateInit() {
        user = new UserEntity(null, "hwang", "asdf", "ROLE_USER", PICTURE_NOVEL, true, "hwang", "woozuda");
        em.persist(user);

        diary1 = Diary.of(user, "asdf", "Diary title1");
        diary2 = Diary.of(user, "asdf", "Diary title2");
        em.persist(diary1);
        em.persist(diary2);

        em.flush();
        em.clear();
    }

    @DisplayName("자유 일기 전체 조회 - 조회된 DTO 가 있는 경우")
    @Test
    void searchCommonNoteListTest_withoutDateCondition_dtoExist() {
        // given
        CommonNoteData result = initCommonNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchCommonNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.commonNote1().getId(), diary1.getId(), diary1.getTitle(),
                                result.commonNote1().getTitle(), result.commonNote1().getDate().toString(),
                                result.commonNote1().getWeather().name(), result.commonNote1().getSeason().name(),
                                result.commonNote1().getFeeling().name(), List.of(result.noteContent1().getContent())),
                        new NoteResponseDto(result.commonNote2().getId(), diary2.getId(), diary2.getTitle(),
                                result.commonNote2().getTitle(), result.commonNote2().getDate().toString(),
                                result.commonNote2().getWeather().name(), result.commonNote2().getSeason().name(),
                                result.commonNote2().getFeeling().name(), List.of(result.noteContent2().getContent())),
                        new NoteResponseDto(result.commonNote3().getId(), diary1.getId(), diary1.getTitle(),
                                result.commonNote3().getTitle(), result.commonNote3().getDate().toString(),
                                result.commonNote3().getWeather().name(), result.commonNote3().getSeason().name(),
                                result.commonNote3().getFeeling().name(), List.of(result.noteContent3().getContent())),
                        new NoteResponseDto(result.commonNote4().getId(), diary2.getId(), diary2.getTitle(),
                                result.commonNote4().getTitle(), result.commonNote4().getDate().toString(),
                                result.commonNote4().getWeather().name(), result.commonNote4().getSeason().name(),
                                result.commonNote4().getFeeling().name(), List.of(result.noteContent4().getContent()))
                );
    }

    @DisplayName("자유 일기 전체 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchCommonNoteListTest_withoutDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchCommonNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    @DisplayName("자유 일기 날짜별 조회 - 조회된 DTO 가 있는 경우")
    @Test
    void searchCommonNoteListTest_withDateCondition_dtoExist() {
        // given
        CommonNoteData result = initCommonNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchCommonNoteList(idList, new NoteCondRequestDto(date2));

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.commonNote3.getId(), diary1.getId(), diary1.getTitle(),
                                result.commonNote3.getTitle(), result.commonNote3.getDate().toString(),
                                result.commonNote3.getWeather().name(), result.commonNote3.getSeason().name(),
                                result.commonNote3.getFeeling().name(), List.of(result.noteContent3.getContent())),
                        new NoteResponseDto(result.commonNote4.getId(), diary2.getId(), diary2.getTitle(),
                                result.commonNote4.getTitle(), result.commonNote4.getDate().toString(),
                                result.commonNote4.getWeather().name(), result.commonNote4.getSeason().name(),
                                result.commonNote4.getFeeling().name(), List.of(result.noteContent4.getContent()))
                );
    }

    @DisplayName("자유 일기 날짜별 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchCommonNoteListTest_withDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchCommonNoteList(idList, new NoteCondRequestDto(date1));

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    @DisplayName("오늘의 질문 일기 조회 - 조회된 DTO가 있는 경우")
    @Test
    void searchQuestionNoteListTest_withoutDateCondition_dtoExist() {
        // given
        QuestionNoteData result = initQuestionNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchQuestionNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.questionNote1().getId(), diary1.getId(), diary1.getTitle(),
                                result.questionNote1().getTitle(), result.questionNote1().getDate().toString(),
                                result.questionNote1().getWeather().name(), result.questionNote1().getSeason().name(),
                                result.questionNote1().getFeeling().name(), result.question1().getContent(), List.of(result.noteContent1().getContent())),
                        new NoteResponseDto(result.questionNote2().getId(), diary2.getId(), diary2.getTitle(),
                                result.questionNote2().getTitle(), result.questionNote2().getDate().toString(),
                                result.questionNote2().getWeather().name(), result.questionNote2().getSeason().name(),
                                result.questionNote2().getFeeling().name(), result.question1().getContent(), List.of(result.noteContent2().getContent())),
                        new NoteResponseDto(result.questionNote3().getId(), diary1.getId(), diary1.getTitle(),
                                result.questionNote3().getTitle(), result.questionNote3().getDate().toString(),
                                result.questionNote3().getWeather().name(), result.questionNote3().getSeason().name(),
                                result.questionNote3().getFeeling().name(), result.question2().getContent(), List.of(result.noteContent3().getContent())),
                        new NoteResponseDto(result.questionNote4().getId(), diary2.getId(), diary2.getTitle(),
                                result.questionNote4().getTitle(), result.questionNote4().getDate().toString(),
                                result.questionNote4().getWeather().name(), result.questionNote4().getSeason().name(),
                                result.questionNote4().getFeeling().name(), result.question2().getContent(), List.of(result.noteContent4().getContent()))
                );

    }

    @DisplayName("오늘의 질문 일기 전체 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchQuestionNoteListTest_withoutDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchQuestionNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    @DisplayName("오늘의 질문 일기 날짜별 조회 - 조회된 DTO가 있는 경우")
    @Test
    void searchQuestionNoteListTest_withDateCondition_dtoExist() {
        // given
        QuestionNoteData result = initQuestionNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchQuestionNoteList(idList, new NoteCondRequestDto(date1));

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.questionNote1.getId(), diary1.getId(), diary1.getTitle(),
                                result.questionNote1.getTitle(), result.questionNote1.getDate().toString(),
                                result.questionNote1.getWeather().name(), result.questionNote1.getSeason().name(),
                                result.questionNote1.getFeeling().name(), result.question1.getContent(), List.of(result.noteContent1.getContent())),
                        new NoteResponseDto(result.questionNote2.getId(), diary2.getId(), diary2.getTitle(),
                                result.questionNote2.getTitle(), result.questionNote2.getDate().toString(),
                                result.questionNote2.getWeather().name(), result.questionNote2.getSeason().name(),
                                result.questionNote2.getFeeling().name(), result.question1.getContent(), List.of(result.noteContent2.getContent()))
                );

    }

    @DisplayName("오늘의 질문 일기 날짜별 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchQuestionNoteListTest_withDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchQuestionNoteList(idList, new NoteCondRequestDto(date1));

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    @DisplayName("회고 일기 전체 조회 - 조회된 DTO 가 있는 경우")
    @Test
    void searchRetrospectiveNoteListTest_withoutDateCondition_dtoExist() {
        // given
        RetrospectiveNoteData result = initRetrospectiveNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchRetrospectiveNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.retrospectiveNote1().getId(), diary1.getId(), diary1.getTitle(),
                                result.retrospectiveNote1().getTitle(), result.retrospectiveNote1().getDate().toString(),
                                result.retrospectiveNote1().getType().name(),
                                List.of(result.noteContent1().getContent(), result.noteContent2().getContent(), result.noteContent3().getContent(), result.noteContent4().getContent())),
                        new NoteResponseDto(result.retrospectiveNote2().getId(), diary2.getId(), diary2.getTitle(),
                                result.retrospectiveNote2().getTitle(), result.retrospectiveNote2().getDate().toString(),
                                result.retrospectiveNote2().getType().name(),
                                List.of(result.noteContent5().getContent(), result.noteContent6().getContent(), result.noteContent7().getContent())),
                        new NoteResponseDto(result.retrospectiveNote3().getId(), diary2.getId(),diary2.getTitle(),
                                result.retrospectiveNote3().getTitle(), result.retrospectiveNote3().getDate().toString(),
                                result.retrospectiveNote3().getType().name(),
                                List.of(result.noteContent8().getContent(), result.noteContent9().getContent(), result.noteContent10().getContent())),
                        new NoteResponseDto(result.retrospectiveNote4().getId(), diary1.getId(),diary1.getTitle(),
                                result.retrospectiveNote4().getTitle(), result.retrospectiveNote4().getDate().toString(),
                                result.retrospectiveNote4().getType().name(),
                                List.of(result.noteContent11().getContent(), result.noteContent12().getContent(), result.noteContent13().getContent()))
                );
    }

    @DisplayName("회고 전체 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchRetrospectiveNoteListTest_withoutDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchRetrospectiveNoteList(idList, new NoteCondRequestDto());

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    @DisplayName("회고 일기 날짜별 조회 - 조회된 DTO 가 있는 경우")
    @Test
    void searchRetrospectiveNoteListTest_withDateCondition_dtoExist() {
        // given
        RetrospectiveNoteData result = initRetrospectiveNoteData();
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchRetrospectiveNoteList(idList, new NoteCondRequestDto(date2));

        // then
        assertThat(responseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new NoteResponseDto(result.retrospectiveNote1.getId(), diary1.getId(), diary1.getTitle(),
                                result.retrospectiveNote1.getTitle(), result.retrospectiveNote1.getDate().toString(),
                                result.retrospectiveNote1.getType().name(),
                                List.of(result.noteContent1.getContent(), result.noteContent2.getContent(), result.noteContent3.getContent(), result.noteContent4.getContent())),
                        new NoteResponseDto(result.retrospectiveNote4.getId(), diary1.getId(),diary1.getTitle(),
                                result.retrospectiveNote4.getTitle(), result.retrospectiveNote4.getDate().toString(),
                                result.retrospectiveNote4.getType().name(),
                                List.of(result.noteContent11.getContent(), result.noteContent12.getContent(), result.noteContent13.getContent()))
                );
    }

    @DisplayName("회고 날짜별 조회 - 조회된 DTO 가 없는 경우")
    @Test
    void searchRetrospectiveNoteListTest_withDateCondition_dtoNotExist() {
        // given
        List<Long> idList = List.of(diary1.getId(), diary2.getId());

        // when
        List<NoteResponseDto> responseDtoList = noteRepository.searchRetrospectiveNoteList(idList, new NoteCondRequestDto(date2));

        // then
        assertThat(responseDtoList).isNotNull();
        assertThat(responseDtoList).hasSize(0);
    }

    private CommonNoteData initCommonNoteData() {
        CommonNote commonNote1 = CommonNote.of(diary1, "Common Note Title1", date1, PRIVATE, CONTENT, SUNNY, WINTER);
        CommonNote commonNote2 = CommonNote.of(diary2, "Common Note Title2", date1, PRIVATE, JOY, SNOW, WINTER);
        CommonNote commonNote3 = CommonNote.of(diary1, "Common Note Title3", date2, PRIVATE, NEUTRAL, CLOUDY, FALL);
        CommonNote commonNote4 = CommonNote.of(diary2, "Common Note Title4", date2, PRIVATE, DISSATISFACTION, SUNNY, FALL);
        em.persist(commonNote1);
        em.persist(commonNote2);
        em.persist(commonNote3);
        em.persist(commonNote4);

        NoteContent noteContent1 = NoteContent.of(commonNote1, 1, "Common Note1 Content");
        NoteContent noteContent2 = NoteContent.of(commonNote2, 1, "Common Note2 Content");
        NoteContent noteContent3 = NoteContent.of(commonNote3, 1, "Common Note3 Content");
        NoteContent noteContent4 = NoteContent.of(commonNote4, 1, "Common Note4 Content");
        em.persist(noteContent1);
        em.persist(noteContent2);
        em.persist(noteContent3);
        em.persist(noteContent4);

        em.flush();
        em.clear();

        return new CommonNoteData(commonNote1, commonNote2, commonNote3, commonNote4, noteContent1, noteContent2, noteContent3, noteContent4);
    }

    private QuestionNoteData initQuestionNoteData() {
        Question question1 = Question.of("Today's Question1");
        Question question2 = Question.of("Today's Question2");
        em.persist(question1);
        em.persist(question2);

        QuestionNote questionNote1 = QuestionNote.of(diary1, "Question Note Title1", date1, PUBLIC, question1, ANGER, CLOUDY, FALL);
        QuestionNote questionNote2 = QuestionNote.of(diary2, "Question Note Title2", date1, PRIVATE, question1, TIREDNESS, THUNDERSTORM, WINTER);
        QuestionNote questionNote3 = QuestionNote.of(diary1, "Question Note Title3", date2, PUBLIC, question2, DISSATISFACTION, RAIN, FALL);
        QuestionNote questionNote4 = QuestionNote.of(diary2, "Question Note Title4", date2, PRIVATE, question2, SADNESS, CLEAR, WINTER);
        em.persist(questionNote1);
        em.persist(questionNote2);
        em.persist(questionNote3);
        em.persist(questionNote4);

        NoteContent noteContent1 = NoteContent.of(questionNote1, 1, "Question Note1 Content");
        NoteContent noteContent2 = NoteContent.of(questionNote2, 1, "Question Note2 Content");
        NoteContent noteContent3 = NoteContent.of(questionNote3, 1, "Question Note3 Content");
        NoteContent noteContent4 = NoteContent.of(questionNote4, 1, "Question Note4 Content");
        em.persist(noteContent1);
        em.persist(noteContent2);
        em.persist(noteContent3);
        em.persist(noteContent4);

        em.flush();
        em.clear();
        QuestionNoteData result = new QuestionNoteData(question1, question2, questionNote1, questionNote2, questionNote3, questionNote4, noteContent1, noteContent2, noteContent3, noteContent4);
        return result;
    }

    private RetrospectiveNoteData initRetrospectiveNoteData() {
        RetrospectiveNote retrospectiveNote1 = RetrospectiveNote.of(diary1, "Retrospective Note Title1", date2, PRIVATE, FOUR_F_S);
        RetrospectiveNote retrospectiveNote2 = RetrospectiveNote.of(diary2, "Retrospective Note Title2", date1, PRIVATE, PMI);
        RetrospectiveNote retrospectiveNote3 = RetrospectiveNote.of(diary2, "Retrospective Note Title3", date1, PRIVATE, KPT);
        RetrospectiveNote retrospectiveNote4 = RetrospectiveNote.of(diary1, "Retrospective Note Title4", date2, PRIVATE, SCS);
        em.persist(retrospectiveNote1);
        em.persist(retrospectiveNote2);
        em.persist(retrospectiveNote3);
        em.persist(retrospectiveNote4);

        NoteContent noteContent1 = NoteContent.of(retrospectiveNote1, 1, "Retrospective Note1 Content1");
        NoteContent noteContent2 = NoteContent.of(retrospectiveNote1, 2, "Retrospective Note1 Content2");
        NoteContent noteContent3 = NoteContent.of(retrospectiveNote1, 3, "Retrospective Note1 Content3");
        NoteContent noteContent4 = NoteContent.of(retrospectiveNote1, 4, "Retrospective Note1 Content4");
        NoteContent noteContent5 = NoteContent.of(retrospectiveNote2, 1, "Retrospective Note2 Content1");
        NoteContent noteContent6 = NoteContent.of(retrospectiveNote2, 2, "Retrospective Note2 Content2");
        NoteContent noteContent7 = NoteContent.of(retrospectiveNote2, 3, "Retrospective Note2 Content3");
        NoteContent noteContent8 = NoteContent.of(retrospectiveNote3, 1, "Retrospective Note3 Content1");
        NoteContent noteContent9 = NoteContent.of(retrospectiveNote3, 2, "Retrospective Note3 Content2");
        NoteContent noteContent10 = NoteContent.of(retrospectiveNote3, 3, "Retrospective Note3 Content3");
        NoteContent noteContent11 = NoteContent.of(retrospectiveNote4, 1, "Retrospective Note4 Content1");
        NoteContent noteContent12 = NoteContent.of(retrospectiveNote4, 2, "Retrospective Note4 Content2");
        NoteContent noteContent13 = NoteContent.of(retrospectiveNote4, 3, "Retrospective Note4 Content3");
        em.persist(noteContent1);
        em.persist(noteContent2);
        em.persist(noteContent3);
        em.persist(noteContent4);
        em.persist(noteContent6);
        em.persist(noteContent5);
        em.persist(noteContent8);
        em.persist(noteContent7);
        em.persist(noteContent9);
        em.persist(noteContent10);
        em.persist(noteContent11);
        em.persist(noteContent12);
        em.persist(noteContent13);

        em.flush();
        em.clear();
        RetrospectiveNoteData result = new RetrospectiveNoteData(retrospectiveNote1, retrospectiveNote2, retrospectiveNote3, retrospectiveNote4, noteContent1, noteContent2, noteContent3, noteContent4, noteContent5, noteContent6, noteContent7, noteContent8, noteContent9, noteContent10, noteContent11, noteContent12, noteContent13);
        return result;
    }

    private record CommonNoteData(CommonNote commonNote1, CommonNote commonNote2, CommonNote commonNote3,
                                  CommonNote commonNote4, NoteContent noteContent1, NoteContent noteContent2,
                                  NoteContent noteContent3, NoteContent noteContent4) {
    }

    private record QuestionNoteData(Question question1, Question question2, QuestionNote questionNote1,
                                    QuestionNote questionNote2, QuestionNote questionNote3, QuestionNote questionNote4,
                                    NoteContent noteContent1, NoteContent noteContent2, NoteContent noteContent3,
                                    NoteContent noteContent4) {
    }

    private record RetrospectiveNoteData(RetrospectiveNote retrospectiveNote1, RetrospectiveNote retrospectiveNote2,
                                         RetrospectiveNote retrospectiveNote3, RetrospectiveNote retrospectiveNote4,
                                         NoteContent noteContent1, NoteContent noteContent2, NoteContent noteContent3,
                                         NoteContent noteContent4, NoteContent noteContent5, NoteContent noteContent6,
                                         NoteContent noteContent7, NoteContent noteContent8, NoteContent noteContent9,
                                         NoteContent noteContent10, NoteContent noteContent11,
                                         NoteContent noteContent12, NoteContent noteContent13) {
    }
}