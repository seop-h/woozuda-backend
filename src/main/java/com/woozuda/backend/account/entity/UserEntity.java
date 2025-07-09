package com.woozuda.backend.account.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.woozuda.backend.account.dto.JoinDTO;
import com.woozuda.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="woozuda_user")
@Entity
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    private String password;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(length = 255, name = "ai_type", nullable = false)
    private AiType aiType;

    //알람 on off
    @Column
    private Boolean alarm;

    //이메일 주소
    @Column
    private String email;

    //가입 경로
    @Column
    private String provider;

    public static UserEntity transEntity(JoinDTO joinDTO){
        return new UserEntity(null, joinDTO.getUsername(), joinDTO.getPassword(), "ROLE_USER", AiType.PICTURE_NOVEL, true, joinDTO.getUsername(), "woozuda");
    }
}
