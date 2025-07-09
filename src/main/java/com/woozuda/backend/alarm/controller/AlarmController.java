package com.woozuda.backend.alarm.controller;

import com.woozuda.backend.account.dto.CustomUser;
import com.woozuda.backend.alarm.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/connect")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUser customUser){
        String username = customUser.getUsername();
        return alarmService.connect(username);
    }

    //프론트 테스트용 api - 매번 일기 3개 만들기가 쉽지 않을 것 같아서 제작.
    @GetMapping("/connect/test")
    public ResponseEntity<Void> alarmTest(@AuthenticationPrincipal CustomUser customUser){
        String username = customUser.getUsername();
        alarmService.alarmTest(username);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
