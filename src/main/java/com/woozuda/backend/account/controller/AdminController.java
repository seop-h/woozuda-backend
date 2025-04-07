package com.woozuda.backend.account.controller;


import com.woozuda.backend.aop.LogExecutionTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/account/sample/admin")
    public String adminP(){
        return "admin controller";
    }

    @LogExecutionTime
    @GetMapping("/account/sample/alluser")
    public String allP(){
        return "all user can access this page!";
    }

    @GetMapping("/account/sample/user")
    public String userP(){ return "user can access this page! ";}

    @GetMapping("/health")
    public String healthcheck() {
        return "OK";
    }
}
