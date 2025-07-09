package com.woozuda.backend.security.filter;

import com.woozuda.backend.security.jwt.IPCheckFilter;
import com.woozuda.backend.security.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

//IPCheckFilter 를 단위 테스트
@ExtendWith(MockitoExtension.class)
public class IPCheckFilterUnitTest {

    private IPCheckFilter ipCheckFilter;

    @Mock
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        List<String> adminIps = List.of("127.0.0.1");
        ipCheckFilter = new IPCheckFilter(adminIps);
    }

    @Test
    void IPFilter_성공_허가ip() throws Exception{

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");

        ipCheckFilter.doFilter(request, response, filterChain);

        verify(response, times(0)).sendError(HttpServletResponse.SC_FORBIDDEN, "IP not allowed");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void IPFilter_실패_비허가ip() throws Exception{

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");

        ipCheckFilter.doFilter(request, response, filterChain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN, "IP not allowed");
        verify(filterChain, times(0)).doFilter(request, response);
    }



}
