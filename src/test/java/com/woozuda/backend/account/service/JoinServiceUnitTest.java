package com.woozuda.backend.account.service;


import com.woozuda.backend.account.dto.JoinDTO;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.exception.account.InvalidEmailException;
import com.woozuda.backend.exception.account.UsernameAlreadyExistsException;
import com.woozuda.backend.shortlink.util.ShortLinkUtil;
import com.woozuda.backend.testutil.UserEntityBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 회원가입 단위 테스트.
@ExtendWith(MockitoExtension.class)
public class JoinServiceUnitTest {

    @InjectMocks
    JoinService joinService;

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    ShortLinkUtil shortLinkUtil;

    @Test
    void 회원가입_성공(){

        //given
        UserEntity userEntity = UserEntityBuilder.createUniqueUser().build();

        // userRepository 에 UserEntity 를 넣었을 때 userEntity 를 반환하는 것으로 가정. 이미 다른 곳에서 검증 된 기능이라고 가정 한다.
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(bCryptPasswordEncoder.encode(userEntity.getPassword())).thenReturn("!@#$");
        doNothing().when(shortLinkUtil).saveShortLink(any(UserEntity.class));

        //when (joinService.joinProcess 실험)
        JoinDTO joinDTO = joinService.joinProcess(new JoinDTO(userEntity.getUsername(), userEntity.getPassword()));

        //then
        assertEquals(userEntity.getUsername(), joinDTO.getUsername());
        assertEquals(userEntity.getPassword(), joinDTO.getPassword());

        verify(userRepository, times(1)).existsByUsername(userEntity.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void 회원가입_실패_중복유저(){

        //given
        UserEntity userEntity = UserEntityBuilder.createUniqueUser().build();

        // userRepository 에 UserEntity 를 넣었을 때 userEntity 를 반환하는 것으로 가정. 이미 다른 곳에서 검증 된 기능이라고 가정 한다.
        when(userRepository.existsByUsername(userEntity.getUsername())).thenReturn(true);

        //when (joinService.joinProcess 실험)
        Exception exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            JoinDTO joinDTO = joinService.joinProcess(new JoinDTO(userEntity.getUsername(), userEntity.getPassword()));
        });

        //then
        // 특정 예외가 발생하는지 검증
        String expectedMessage = "이미 존재하는 회원 입니다";
        String actualMessage = exception.getMessage();
        assertTrue(exception.getMessage().contains(expectedMessage));

        //save 호출 되면 안됨
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void 회원가입_실패_이메일형태틀림(){

        //given
        UserEntity userEntity = UserEntityBuilder.createUniqueUser().withUsername("test").build();

        //when (joinService.joinProcess 실험)
        Exception exception = assertThrows(InvalidEmailException.class, () -> {
            JoinDTO joinDTO = joinService.joinProcess(new JoinDTO(userEntity.getUsername(), userEntity.getPassword()));
        });

        //then
        // 특정 예외가 발생하는지 검증
        String expectedMessage = "잘못된 이메일 형식을 입력 했습니다";
        String actualMessage = exception.getMessage();
        assertTrue(exception.getMessage().contains(expectedMessage));

        //save 호출 되면 안됨
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

}
