package com.woozuda.backend.security.login;

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


// 로그인(/login) 에 대한 통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortLinkRepository shortLinkRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp(){
        UserEntityBuilder.resetCounter();
        shortLinkRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(UserEntityBuilder.createUniqueUser().build());
    }


    @Test
    public void 로그인_성공() throws Exception {

        //given
        String requestBody = "{\"username\":\"user1@gmail.com\", \"password\":\"1234\"}";

        //when
        MvcResult mvcResult = mockMvc.perform(post("/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(requestBody))
                                    .andExpect(status().isOk())
                                    .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie[] cookies = response.getCookies();

        String authorization = "";
        for (Cookie cookie : cookies) {

            //System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //then ( 정상적인 토큰이 발급 되었는지 확인 )
        assertThat(jwtUtil.getUsername(authorization)).isEqualTo("user1@gmail.com");
        assertThat(jwtUtil.isExpired(authorization)).isEqualTo(false);

    }

    @Test
    public void 로그인_실패_없는계정() throws Exception {

        //given
        String requestBody = "{\"username\":\"no@gmail.com\", \"password\":\"1234\"}";

        //when
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie[] cookies = response.getCookies();

        String authorization = "";
        for (Cookie cookie : cookies) {

            //System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //then ( 정상적인 토큰이 발급 되었는지 확인 )
        assertThat(authorization).isEqualTo("");
    }

    @Test
    public void 로그인_실패_비밀번호틀림() throws Exception {

        //given
        String requestBody = "{\"username\":\"user1@gmail.com\", \"password\":\"5555\"}";

        //when
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie[] cookies = response.getCookies();

        String authorization = "";
        for (Cookie cookie : cookies) {

            //System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //then ( 정상적인 토큰이 발급 되었는지 확인 )
        assertThat(authorization).isEqualTo("");
    }


    @Test
    public void 로그인_실패_empty_json() throws Exception {

        //given
        String requestBody = "{}";

        //when
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie[] cookies = response.getCookies();

        String authorization = "";
        for (Cookie cookie : cookies) {

            //System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //then ( 정상적인 토큰이 발급 되었는지 확인 )
        assertThat(authorization).isEqualTo("");
    }
}
