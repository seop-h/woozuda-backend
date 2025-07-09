package com.woozuda.backend.account.service;

import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.security.jwt.JWTUtil;
import com.woozuda.backend.shortlink.repository.ShortLinkRepository;
import com.woozuda.backend.testutil.UserEntityBuilder;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 회원가입 통합 테스트.
@SpringBootTest
@AutoConfigureMockMvc
public class JoinTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortLinkRepository shortLinkRepository;

    @BeforeEach
    void setUp(){
        UserEntityBuilder.resetCounter();
        shortLinkRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(UserEntityBuilder.createUniqueUser().build());
    }

    @Test
    public void 회원가입_성공() throws Exception {

        //given
        String requestBody = "{\"username\":\"user2@gmail.com\", \"password\":\"1234\"}";

        //when
        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    public void 회원가입_실패_중복유저() throws Exception {

        //given
        String requestBody = "{\"username\":\"user1@gmail.com\", \"password\":\"1234\"}";

        //when
        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    public void 회원가입_실패_잘못된형식() throws Exception {

        //given
        String requestBody = "{\"username\":\"user1\", \"password\":\"1234\"}";

        //when
        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }


}
