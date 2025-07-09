package com.woozuda.backend.account.dto;


import com.woozuda.backend.account.entity.AiType;
import com.woozuda.backend.account.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JoinDTO {
    String username;
    String password;

    public static JoinDTO transDTO(UserEntity userEntity){
        return new JoinDTO(userEntity.getUsername(), userEntity.getPassword());
    }
}
