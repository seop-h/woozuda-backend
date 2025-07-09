package com.woozuda.backend.shortlink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.note.entity.*;
import com.woozuda.backend.note.entity.type.*;
import com.woozuda.backend.note.repository.NoteRepository;
import com.woozuda.backend.shortlink.dto.note.NoteIdDto;
import com.woozuda.backend.shortlink.dto.note.SharedNoteResponseDto;
import com.woozuda.backend.shortlink.entity.ShortLink;
import com.woozuda.backend.shortlink.repository.ShortLinkRepository;
import com.woozuda.backend.testutil.UserEntityBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.woozuda.backend.account.entity.AiType.PICTURE_NOVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ShareServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ShortLinkRepository shortLinkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShareService shareService;


    // 각 테스트 전에 noteRepository
    @BeforeEach
    void setUp(){
        //userRepository.deleteAll();
        //diaryRepository.deleteAll();
        //noteRepository.deleteAll();
    }

    @WithMockUser(username = "woozuda@gmail.com", roles = {"ADMIN"})
    @DisplayName("노트 공유 시 visibility 칼럼이 Public 으로 바뀌는지 검사")
    @Test
    void makeSharedNoteTest() throws Exception {

        //given - 데이터 넣기 (user 1명, diary 1개, question 1개 ,note 5개)

        // before
        // UserEntity user = new UserEntity(null, "woozuda@gmail.com", "1234", "ROLE_ADMIN", PICTURE_NOVEL, true, "woozuda@gmail.com", "woozuda");

        // after
        UserEntity user = UserEntityBuilder.createUniqueUser()
                                            .withPassword("1234")
                                            .withEmail("woozuda@gmail.com")
                                            .build();

        userRepository.save(user);

        Diary diary1 = Diary.of(user, "https://woozuda-image.kr.object.ncloudstorage.com/random-image-1.jpg", "my first diary");
        diaryRepository.save(diary1);

        Note note1 = CommonNote.of(diary1, "나는 노트 1이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note2 = CommonNote.of(diary1, "나는 노트 2이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note3 = RetrospectiveNote.of(diary1, "나는 노트 3", LocalDate.now(), Visibility.PRIVATE, Framework.KPT);
        Note note4 = CommonNote.of(diary1, "나는 노트 4이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note5 = CommonNote.of(diary1, "나는 노트 5이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note6 = RetrospectiveNote.of(diary1, "나는 노트 6", LocalDate.now(), Visibility.PRIVATE, Framework.KPT);

        noteRepository.saveAll(Arrays.asList(note1, note2, note3, note4, note5, note6));

        //when
        NoteIdDto noteIdDto = new NoteIdDto();
        noteIdDto.setId(Arrays.asList(1L, 3L, 5L));

        ResultActions perform = mockMvc.perform(post("/api/shared/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteIdDto)))
                .andExpect(status().isOk())
                .andDo(print());

        //then - 데이터 값 확인.
        Note changedNote1 = noteRepository.findById(1L).orElse(null);
        Note changedNote2 = noteRepository.findById(2L).orElse(null);
        Note changedNote3 = noteRepository.findById(3L).orElse(null);
        Note changedNote4 = noteRepository.findById(4L).orElse(null);
        Note changedNote5 = noteRepository.findById(5L).orElse(null);
        Note changedNote6 = noteRepository.findById(6L).orElse(null);

        assertEquals(Visibility.PUBLIC, changedNote1.getVisibility());
        assertEquals(Visibility.PUBLIC, changedNote3.getVisibility());
        assertEquals(Visibility.PUBLIC, changedNote5.getVisibility());

        assertEquals(Visibility.PRIVATE, changedNote2.getVisibility());
        assertEquals(Visibility.PRIVATE, changedNote4.getVisibility());
        assertEquals(Visibility.PRIVATE, changedNote6.getVisibility());
    }


    @WithMockUser(username = "woozuda@gmail.com", roles = {"ADMIN"})
    @DisplayName("노트 공유 시 visibility 칼럼이 Public 으로 바뀌는지 검사")
    @Test
    void getSharedNoteTest() throws Exception {

        //given - 데이터 넣기 (user 1명, diary 1개, question 1개 ,note 5개)

        //Before
        //UserEntity user1 = new UserEntity(null, "woozuda@gmail.com", "1234", "ROLE_ADMIN", PICTURE_NOVEL, true, "woozuda@gmail.com", "woozuda");
        //UserEntity user2 = new UserEntity(null, "rodom1018@gmail.com", "1234", "ROLE_ADMIN", PICTURE_NOVEL, true, "rodom1018@gmail.com", "woozuda");
        //userRepository.save(user1);
        //userRepository.save(user2);

        //after
        List<UserEntity> userEntityList = UserEntityBuilder.createUniqueMultipleUser(10);
        userRepository.saveAll(userEntityList);

        Diary diary1 = Diary.of(userEntityList.get(0), "https://woozuda-image.kr.object.ncloudstorage.com/random-image-1.jpg", "my first diary");
        Diary diary2 = Diary.of(userEntityList.get(1), "https://woozuda-image.kr.object.ncloudstorage.com/random-image-1.jpg", "my first diary22");
        diaryRepository.save(diary1);
        diaryRepository.save(diary2);

        Note note1 = CommonNote.of(diary1, "나는 노트 1이다", LocalDate.now(), Visibility.PUBLIC, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note2 = CommonNote.of(diary1, "나는 노트 2이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note3 = RetrospectiveNote.of(diary1, "나는 노트 3", LocalDate.now(), Visibility.PUBLIC, Framework.KPT);
        Note note4 = CommonNote.of(diary2, "나는 노트 4이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note5 = CommonNote.of(diary2, "나는 노트 5이다", LocalDate.now(), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note6 = RetrospectiveNote.of(diary2, "나는 노트 6", LocalDate.now(), Visibility.PRIVATE, Framework.KPT);

        noteRepository.saveAll(Arrays.asList(note1, note2, note3, note4, note5, note6));

        //when
        SharedNoteResponseDto sharedNoteResponseDto= shareService.getSharedNote("woozuda@gmail.com");


        //then - 데이터 값 확인
    }

    @Test
    void getShortLinkTest(){

        UserEntity user1 = new UserEntity(null, "woozuda@gmail.com", "1234", "ROLE_ADMIN", PICTURE_NOVEL, true, "woozuda@gmail.com", "woozuda");
        userRepository.save(user1);

        shortLinkRepository.save(new ShortLink(null, "asdfasdf", user1));

        if(shortLinkRepository.findByUrl("asdfasdf") != null){
            System.out.println("정상");
        }
        System.out.println("하이");

    }
}
