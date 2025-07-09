package com.woozuda.backend.diary.repository;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.diary.dto.response.SingleDiaryResponseDto;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.note.entity.converter.AesEncryptor;
import com.woozuda.backend.tag.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.woozuda.backend.account.entity.AiType.PICTURE_NOVEL;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@MockBean(AesEncryptor.class)
@Transactional
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private TestEntityManager em;

    @DisplayName("다이어리 저장")
    @Test
    void givenUserId_whenSaveDiary_thenReturnsCorrect() {
        // Given
        UserEntity user = new UserEntity(null, "hwang", "qwe123!", "ROLE_USER", PICTURE_NOVEL, true, "hwang", "woozuda");

        em.persist(user);

        Tag tag1 = Tag.of("tag1");
        Tag tag2 = Tag.of("tag2");

        em.persist(tag1);
        em.persist(tag2);

        em.flush();
        em.clear();

        // When
        Diary diary = Diary.of(user, "imgUrl", "diary title");

        diary.addTag(tag1);
        diary.addTag(tag2);
        diaryRepository.save(diary);

        // Then
        Optional<Diary> foundDiary = diaryRepository.findById(diary.getId());
        assertThat(foundDiary).isPresent();
        assertThat(foundDiary.get().getTagList()).hasSize(2);
    }

    @DisplayName("다이어리 전체 조회")
    @Test
    void searchDiarySummaryList_ShouldReturnExpectedResults() {
        //Given
        UserEntity user = new UserEntity(null, "test_user", "password", "ROLE_USER", PICTURE_NOVEL, true, "test_user", "woozuda");
        em.persist(user);

        Tag tag1 = Tag.of("Tag1");
        Tag tag2 = Tag.of("Tag2");
        Tag tag3 = Tag.of("Tag3");
        Tag tag4 = Tag.of("Tag4");

        em.persist(tag1);
        em.persist(tag2);
        em.persist(tag3);
        em.persist(tag4);

        Diary diary1 = Diary.of(user, "image1_url", "Diary1 Title");
        diary1.change("Updated Diary1 Title", List.of(tag1, tag2), "new_image1_url");
        em.persist(diary1);

        Diary diary2 = Diary.of(user, "image2_url", "Diary2 Title");
        diary2.change("Updated Diary2 Title", List.of(tag3, tag4), "new_image2_url");
        em.persist(diary2);

        em.flush();
        em.clear();

        //When
        List<SingleDiaryResponseDto> responseDto = diaryRepository.searchDiarySummaryList("test_user");

        //Then
        assertThat(responseDto).hasSize(2); //diary1, diary2

        SingleDiaryResponseDto firstDiary = responseDto.get(0);
        assertThat(firstDiary.getTitle()).isEqualTo("Updated Diary1 Title");
        assertThat(firstDiary.getSubject()).containsExactlyInAnyOrder("Tag1", "Tag2");
        assertThat(firstDiary.getImgUrl()).isEqualTo("new_image1_url");

        SingleDiaryResponseDto secondDiary = responseDto.get(1);
        assertThat(secondDiary.getTitle()).isEqualTo("Updated Diary2 Title");
        assertThat(secondDiary.getSubject()).containsExactlyInAnyOrder("Tag3", "Tag4");
        assertThat(secondDiary.getImgUrl()).isEqualTo("new_image2_url");
    }

    @DisplayName("단일 다이어리 조회")
    @Test
    void searchSingleDiarySummaryTest() {
        //Given
        UserEntity user = new UserEntity(null, "test_user", "password", "ROLE_USER", PICTURE_NOVEL, true, "test_user", "woozuda");
        em.persist(user);

        Tag tag1 = Tag.of("Tag1");
        Tag tag2 = Tag.of("Tag2");
        Tag tag3 = Tag.of("Tag3");
        Tag tag4 = Tag.of("Tag4");

        em.persist(tag1);
        em.persist(tag2);
        em.persist(tag3);
        em.persist(tag4);

        Diary diary1 = Diary.of(user, "image1_url", "Diary1 Title");
        diary1.change("Updated Diary1 Title", List.of(tag1, tag2), "new_image1_url");
        em.persist(diary1);

        Diary diary2 = Diary.of(user, "image2_url", "Diary2 Title");
        diary2.change("Updated Diary2 Title", List.of(tag3, tag4), "new_image2_url");
        em.persist(diary2);

        em.flush();
        em.clear();

        //When
        SingleDiaryResponseDto responseDto = diaryRepository.searchSingleDiarySummary(user.getUsername(), diary1.getId());

        //Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto)
                .usingRecursiveComparison()
                .isEqualTo(new SingleDiaryResponseDto(
                                diary1.getId(),
                                diary1.getTitle(),
                                List.of(tag1.getName(), tag2.getName()),
                                diary1.getImage(),
                                diary1.getStartDate(),
                                diary1.getEndDate(),
                                diary1.getNoteCount()
                        )
                );

    }

}