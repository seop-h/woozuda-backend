package com.woozuda.backend.account.controller;

import com.woozuda.backend.account.dto.JoinDTO;
import com.woozuda.backend.account.service.JoinService;
import com.woozuda.backend.exception.account.InvalidEmailException;
import com.woozuda.backend.exception.account.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService){
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<Void> joinProcess(@RequestBody JoinDTO joinDTO){
        joinService.joinProcess(joinDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
