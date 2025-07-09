package com.woozuda.backend.shortlink.service;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.ai_creation.entity.AiCreation;
import com.woozuda.backend.ai_creation.entity.CreationType;
import com.woozuda.backend.ai_creation.entity.CreationVisibility;
import com.woozuda.backend.ai_creation.repository.AiCreationRepository;
import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.diary.repository.DiaryRepository;
import com.woozuda.backend.note.entity.*;
import com.woozuda.backend.note.entity.type.*;
import com.woozuda.backend.note.repository.NoteRepository;
import com.woozuda.backend.question.entity.Question;
import com.woozuda.backend.question.repository.QuestionRepository;
import com.woozuda.backend.security.jwt.JWTUtil;
import com.woozuda.backend.shortlink.entity.ShortLink;
import com.woozuda.backend.shortlink.repository.ShortLinkRepository;
import com.woozuda.backend.testutil.UserEntityBuilder;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SharedShortLinkTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShortLinkRepository shortLinkRepository;

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    AiCreationRepository aiCreationRepository;

    @Autowired
    JWTUtil jwtUtil;

    @BeforeEach
    void setUp(){
        //초기화
        noteRepository.deleteAll();
        questionRepository.deleteAll();
        diaryRepository.deleteAll();
        shortLinkRepository.deleteAll();
        aiCreationRepository.deleteAll();
        userRepository.deleteAll();
        UserEntityBuilder.resetCounter();
        // 상황 설정
        // user1@gmail.com 는 공유 일기를 4개 가지고 있음 : 2025-1-1 에 2개 , 2025-1-10 에 2개
        // user1@gmail.com 는 공유 ai 컨텐츠를 가지고 있지 않음
        // user2@gmail.com 는 공유 일기를 가지고 있지 않음
        // user2@gmail.com 는 공유 ai 컨텐츠를 4개 가지고 있음 : 2025-1-1 에 2개, 2025-1-10 에 2개

        // 1. 유저 저장
        List<UserEntity> users = UserEntityBuilder.createUniqueMultipleUser(2);
        userRepository.saveAll(users);

        // 2. 유저에 연결된 숏링크 저장
        ShortLink shortLink1 = new ShortLink(null, "user1hash", users.get(0));
        ShortLink shortLink2 = new ShortLink(null, "user2hash", users.get(1));
        shortLinkRepository.saveAll(Arrays.asList(shortLink1, shortLink2));

        // 3. 다이어리 생성
        Diary diary1 = Diary.of(users.get(0), "https://sample/image", "user1의 다이어리");
        Diary diary2 = Diary.of(users.get(1), "https://sample/image2", "user2의 다이어리");
        diaryRepository.saveAll(Arrays.asList(diary1,diary2));

        // 4-1. 질문 생성
        Question question1 = Question.of("질문1");
        questionRepository.save(question1);

        // 5-1. user1@gmail.com의 일기 생성
        Note note1 = CommonNote.of(diary1, "자유일기1", LocalDate.of(2025, 1, 1), Visibility.PUBLIC, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note2 = CommonNote.of(diary1, "자유일기2", LocalDate.of(2025, 1, 1), Visibility.PUBLIC, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note3 = CommonNote.of(diary1, "자유일기3", LocalDate.of(2025, 1, 1), Visibility.PRIVATE, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note4 = QuestionNote.of(diary1, "질문일기1", LocalDate.of(2025, 1, 10), Visibility.PUBLIC, question1, Feeling.JOY, Weather.SUNNY, Season.WINTER);
        Note note5 = RetrospectiveNote.of(diary1, "회고일기1", LocalDate.of(2025, 1, 10), Visibility.PUBLIC, Framework.KPT);

        List<String> content1 = new ArrayList<>(Arrays.asList("자유일기1-내용"));
        for (int i = 0; i < content1.size(); i++) {
            NoteContent noteContent = NoteContent.of(i + 1, content1.get(i));
            note1.addContent(noteContent);
        }

        List<String> content2 = new ArrayList<>(Arrays.asList("Keep", "Problem", "Try"));
        for (int i = 0; i < content2.size(); i++) {
            NoteContent noteContent = NoteContent.of(i + 1, content2.get(i));
            note5.addContent(noteContent);
        }

        noteRepository.saveAll(Arrays.asList(note1, note2, note3, note4, note5));

        // 5-2. user2@gmail.com 의 ai 컨텐츠 생성
        AiCreation aiCreation1 = new AiCreation(null, users.get(1), CreationType.POETRY,LocalDate.of(2024,12,30),
                LocalDate.of(2025, 1,5), "https://test/sample", "시입니다", CreationVisibility.PUBLIC);
        AiCreation aiCreation2 = new AiCreation(null, users.get(1), CreationType.WRITING,LocalDate.of(2024,12,30),
                LocalDate.of(2025, 1,5), "https://test/sample", "소설입니다", CreationVisibility.PUBLIC);
        AiCreation aiCreation3 = new AiCreation(null, users.get(1), CreationType.POETRY,LocalDate.of(2025,1,6),
                LocalDate.of(2025, 1,12), "https://test/sample", "시입니다", CreationVisibility.PUBLIC);
        AiCreation aiCreation4 = new AiCreation(null, users.get(1), CreationType.WRITING,LocalDate.of(2025,1,6),
                LocalDate.of(2025, 1,12), "https://test/sample", "소설입니다", CreationVisibility.PUBLIC);
        AiCreation aiCreation5 = new AiCreation(null, users.get(1), CreationType.WRITING,LocalDate.of(2025,1,6),
                LocalDate.of(2025, 1,12), "https://test/sample", "소설입니다", CreationVisibility.PRIVATE);

        aiCreationRepository.saveAll(Arrays.asList(aiCreation1, aiCreation2, aiCreation3, aiCreation4, aiCreation5));
    }

    @Test
    void 공유_일기_가져오기() throws Exception{
        String jwtToken = jwtUtil.createJwt("user1@gmail.com", "ROLE_USER", 10000L);
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shared/note")
                                        .cookie(new Cookie("Authorization", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.sharedNotes[0].date").value("2025-01-10"))
                .andExpect(jsonPath("$.sharedNotes[1].date").value("2025-01-01"))
                .andExpect(jsonPath("$.sharedNotes[0].notes[0].type").value("QUESTION"))
                .andExpect(jsonPath("$.sharedNotes[0].notes[1].type").value("RETROSPECTIVE"))
                .andExpect(jsonPath("$.sharedNotes[1].notes[0].type").value("COMMON"))
                .andExpect(jsonPath("$.sharedNotes[1].notes[1].type").value("COMMON"))
                .andDo(print());
    }

    @Test
    void 공유_일기_숏링크_가져오기() throws Exception{
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shortlink/note/user1hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.sharedNotes[0].date").value("2025-01-10"))
                .andExpect(jsonPath("$.sharedNotes[1].date").value("2025-01-01"))
                .andExpect(jsonPath("$.sharedNotes[0].notes[0].type").value("QUESTION"))
                .andExpect(jsonPath("$.sharedNotes[0].notes[1].type").value("RETROSPECTIVE"))
                .andExpect(jsonPath("$.sharedNotes[1].notes[0].type").value("COMMON"))
                .andExpect(jsonPath("$.sharedNotes[1].notes[1].type").value("COMMON"))
                .andDo(print());
    }

    @Test
    void 공유_일기_한개도없을때() throws Exception{
        //user2는 0개의 공유 일기를 가지고 있음
        String jwtToken = jwtUtil.createJwt("user2@gmail.com", "ROLE_USER", 10000L);
        ResultActions perform = mockMvc.perform(get("/api/shared/note")
                        .cookie(new Cookie("Authorization", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andDo(print());
    }

    @Test
    void 공유_일기_숏링크_한개도없을때() throws Exception{
        //user2는 0개의 공유 일기를 가지고 있음
        ResultActions perform = mockMvc.perform(get("/api/shortlink/note/user2hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andDo(print());
    }

    @Test
    void 공유_ai컨텐츠_가져오기() throws Exception{
        System.out.println("여기닷");
        List<AiCreation> aiCreations = aiCreationRepository.findAll();
        for(AiCreation aiCreation : aiCreations){
            System.out.println(aiCreation.getUser().getUsername() +" " +aiCreation.getCreationVisibility());
        }

        String jwtToken = jwtUtil.createJwt("user2@gmail.com", "ROLE_USER", 10000L);
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shared/ai")
                        .cookie(new Cookie("Authorization", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.sharedAiCreations[0].start_date").value("2024-12-30"))
                .andExpect(jsonPath("$.sharedAiCreations[1].start_date").value("2025-01-06"))
                .andExpect(jsonPath("$.sharedAiCreations[0].aiCreations[0].creationType").value("POETRY"))
                .andExpect(jsonPath("$.sharedAiCreations[0].aiCreations[1].creationType").value("WRITING"))
                .andExpect(jsonPath("$.sharedAiCreations[1].aiCreations[0].creationType").value("POETRY"))
                .andExpect(jsonPath("$.sharedAiCreations[1].aiCreations[1].creationType").value("WRITING"))
                .andDo(print());
    }

    @Test
    void 공유_ai컨텐츠_숏링크_가져오기() throws Exception{
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shortlink/ai/user2hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.sharedAiCreations[0].start_date").value("2024-12-30"))
                .andExpect(jsonPath("$.sharedAiCreations[1].start_date").value("2025-01-06"))
                .andExpect(jsonPath("$.sharedAiCreations[0].aiCreations[0].creationType").value("POETRY"))
                .andExpect(jsonPath("$.sharedAiCreations[0].aiCreations[1].creationType").value("WRITING"))
                .andExpect(jsonPath("$.sharedAiCreations[1].aiCreations[0].creationType").value("POETRY"))
                .andExpect(jsonPath("$.sharedAiCreations[1].aiCreations[1].creationType").value("WRITING"))
                .andDo(print());
    }
    @Test
    void 공유_ai컨텐츠_한개도없을때() throws Exception{
        String jwtToken = jwtUtil.createJwt("user1@gmail.com", "ROLE_USER", 10000L);
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shared/ai")
                        .cookie(new Cookie("Authorization", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andDo(print());
    }

    @Test
    void 공유_ai컨텐츠_숏링크_한개도없을때() throws Exception{
        //user1은 4개의 공유 일기를 가지고 있음 ( 2025-1-1 자유일기 2개, 2025-1-10 질문일기 1개 + 회고일기 1개 )
        ResultActions perform = mockMvc.perform(get("/api/shortlink/ai/user1hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andDo(print());
    }
}
