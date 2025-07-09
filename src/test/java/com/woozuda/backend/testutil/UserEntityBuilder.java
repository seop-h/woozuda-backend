package com.woozuda.backend.testutil;

import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import org.h2.engine.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class UserEntityBuilder {

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private Long id = null;
    private String username;
    private String password = "1234";
    private String role = "ROLE_USER";
    private AiType aiType = AiType.PICTURE_NOVEL;
    private Boolean alarm = true;
    private String email = "test@gmail.com";
    private String provider = "woozuda";

    public UserEntityBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserEntityBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserEntityBuilder withRole(String role) {
        this.role = role;
        return this;
    }

    public UserEntityBuilder withAiType(AiType aiType) {
        this.aiType = aiType;
        return this;
    }

    public UserEntityBuilder withAlarm(Boolean alarm) {
        this.alarm = alarm;
        return this;
    }

    public UserEntityBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserEntityBuilder withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public UserEntity build() {
        return new UserEntity(id, username, bCryptPasswordEncoder.encode(password), role, aiType, alarm, email, provider);
    }

    public static void resetCounter() {
        COUNTER.set(0);
    }
    public static UserEntityBuilder createUniqueUser(){
        int count = COUNTER.incrementAndGet();

        UserEntityBuilder userEntityBuilder = new UserEntityBuilder();

        userEntityBuilder.username = "user" + count + "@gmail.com";
        return userEntityBuilder;
    }

    public static List<UserEntity> createUniqueMultipleUser(int count){
        List<UserEntity> userEntityList = new ArrayList<>();
        for(int i=0; i<count; i++){
            userEntityList.add(createUniqueUser().build());
        }
        return userEntityList;
    }
}
