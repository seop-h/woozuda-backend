package com.woozuda.backend.account.service;

//import com.woozuda.backend.account.dto.CustomUserDetails;
import com.woozuda.backend.account.dto.CustomUser;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

//        log.info("로그인 디버깅 로그 - ");
//        log.info(username);
        UserEntity userData = userRepository.findByUsername(username);

        if(userData != null){
            return new CustomUser(userData);
        }

        return null;
    }
}
