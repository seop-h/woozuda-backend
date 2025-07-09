package com.woozuda.backend.security.filter;

import com.woozuda.backend.security.jwt.JWTFilter;
import com.woozuda.backend.security.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

//JWTFilter를 단위 테스트 하기 위한 클래스
@ExtendWith(MockitoExtension.class)
public class JWTFilterUnitTest {

    @InjectMocks
    private JWTFilter jwtFilter;

    @Mock
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void JWTFilter_성공_일반유저() throws Exception{
        //given

        //JWTFilter는 HttpServletRequest request, HttpServletResponse response, FilterChain filterChain 를 인수로 받음 (*OncePerRequestFilter를 상속할 시 해당됨)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        Cookie[] cookies ={
            new Cookie("Authorization", "goodtoken")
        };

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.isExpired("goodtoken")).thenReturn(false);
        when(jwtUtil.getUsername("goodtoken")).thenReturn("gooduser");
        when(jwtUtil.getRole("goodtoken")).thenReturn("ROLE_USER");


        //when
        // doFilter 가 doFilterInternal 을 호출
        jwtFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isEqualTo(true);
        assertThat(authentication.getName()).isEqualTo("gooduser");

        List<String> authorityNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorityNames).contains("ROLE_USER");


        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void JWTFilter_성공_어드민() throws Exception{
        //given

        //JWTFilter는 HttpServletRequest request, HttpServletResponse response, FilterChain filterChain 를 인수로 받음 (*OncePerRequestFilter를 상속할 시 해당됨)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        Cookie[] cookies ={
                new Cookie("Authorization", "goodtoken")
        };

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.isExpired("goodtoken")).thenReturn(false);
        when(jwtUtil.getUsername("goodtoken")).thenReturn("gooduser");
        when(jwtUtil.getRole("goodtoken")).thenReturn("ROLE_ADMIN");


        //when
        // doFilter 가 doFilterInternal 을 호출
        jwtFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isEqualTo(true);
        assertThat(authentication.getName()).isEqualTo("gooduser");

        List<String> authorityNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorityNames).contains("ROLE_ADMIN");


        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void JWTFilter_실패_쿠키null() throws Exception{
        //given

        //JWTFilter는 HttpServletRequest request, HttpServletResponse response, FilterChain filterChain 를 인수로 받음 (*OncePerRequestFilter를 상속할 시 해당됨)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getCookies()).thenReturn(null);


        //when
        // doFilter 가 doFilterInternal 을 호출
        jwtFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void JWTFilter_실패_Authorization이없는쿠키() throws Exception{
        //given

        //JWTFilter는 HttpServletRequest request, HttpServletResponse response, FilterChain filterChain 를 인수로 받음 (*OncePerRequestFilter를 상속할 시 해당됨)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        Cookie[] cookies ={
                new Cookie("A", "goodtoken")
        };

        when(request.getCookies()).thenReturn(cookies);

        //when
        // doFilter 가 doFilterInternal 을 호출
        jwtFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void JWTFilter_실패_만료된토큰() throws Exception{
        //given

        //JWTFilter는 HttpServletRequest request, HttpServletResponse response, FilterChain filterChain 를 인수로 받음 (*OncePerRequestFilter를 상속할 시 해당됨)
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        Cookie[] cookies ={
                new Cookie("Authorization", "expiredtoken")
        };

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.isExpired("expiredtoken")).thenReturn(true);


        //when
        // doFilter 가 doFilterInternal 을 호출
        jwtFilter.doFilter(request, response, filterChain);

        //then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain, times(1)).doFilter(request, response);
    }

}
