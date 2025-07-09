package com.woozuda.backend.security.oauth2;

import com.woozuda.backend.account.dto.CustomUser;
import com.woozuda.backend.security.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomUser customUserDetails = (CustomUser) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 90*24*60*60*1000L);

        ResponseCookie responseCookie = createCookie("Authorization", token);
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        response.sendRedirect("https://woozuda.swygbro.com");
        //response.addCookie(createCookie("Authorization", token));
    }

    private ResponseCookie createCookie(String key, String value) {

        /*
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(90*24*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

         */
        ResponseCookie responseCookie = ResponseCookie.from(key, value)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(90 * 24 * 60 * 60)
                .sameSite("None")
                .build();


        return responseCookie;
    }
}