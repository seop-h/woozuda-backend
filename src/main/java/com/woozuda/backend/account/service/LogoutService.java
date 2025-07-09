package com.woozuda.backend.account.service;


import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    public ResponseCookie logout(){

        ResponseCookie expiredCookie = createCookie("Authorization", "");
        return expiredCookie;
    }

    private ResponseCookie createCookie(String key, String value) {

        ResponseCookie responseCookie = ResponseCookie.from(key, value)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        return responseCookie;
    }
}
