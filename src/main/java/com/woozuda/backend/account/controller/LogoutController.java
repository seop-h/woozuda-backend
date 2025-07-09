package com.woozuda.backend.account.controller;


import com.woozuda.backend.account.service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/logout")
@RequiredArgsConstructor
@RestController
public class LogoutController {

    private final LogoutService logoutService;

    @GetMapping("")
    public ResponseEntity<Void> logout(){

        ResponseCookie expiredCookie = logoutService.logout();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }
}
