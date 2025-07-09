package com.woozuda.backend.security.jwt;

import com.woozuda.backend.account.dto.CustomUser;
//import com.woozuda.backend.account.dto.CustomUserDetails;
import com.woozuda.backend.account.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 헤더 버전
        // String authorization = request.getHeader("Authorization");

        String authorization = null;

        Cookie[] cookies = request.getCookies();

        if (cookies == null){
            filterChain.doFilter(request, response);
            return;
        }

        for (Cookie cookie : cookies) {

            //System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //Authorization 헤더 검증
        if (authorization == null) {

            //System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        String token = authorization;

        // 예외 처리 (토큰 소멸 여부)
        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        //토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("temp");
        userEntity.setRole(role);

        //세션에 사용자 등록 . stateless 로서 이번 1회만 적용 된다 .
        CustomUser customOAuth2User = new CustomUser(userEntity);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //다음 필터를 이어간다
        filterChain.doFilter(request, response);
    }
}
