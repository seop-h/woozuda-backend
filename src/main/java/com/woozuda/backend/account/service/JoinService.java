package com.woozuda.backend.account.service;

import com.woozuda.backend.account.dto.JoinDTO;
import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.account.repository.UserRepository;
import com.woozuda.backend.exception.account.InvalidEmailException;
import com.woozuda.backend.exception.account.UsernameAlreadyExistsException;
import com.woozuda.backend.shortlink.util.ShortLinkUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@RequiredArgsConstructor
@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ShortLinkUtil shortLinkUtil;

    @Transactional
    public JoinDTO joinProcess(JoinDTO joinDTO){

        if(!isValidEmail(joinDTO.getUsername())){
            throw new InvalidEmailException("잘못된 이메일 형식을 입력 했습니다");
        }

        //이미 있는 계정이면 만들 수 없습니다
        if(userRepository.existsByUsername(joinDTO.getUsername())){
            throw new UsernameAlreadyExistsException("이미 존재하는 회원 입니다");
        }

        //비밀번호 암호화(bcrypt)
        joinDTO.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));

        //dto -> entity 변환
        UserEntity data = UserEntity.transEntity(joinDTO);

        //레포지터리에 entity를 저장합니다
        UserEntity newUser = userRepository.save(data);

        // 유저에 대한 숏링크 제작
        shortLinkUtil.saveShortLink(data);

        return JoinDTO.transDTO(newUser);
    }

    public boolean isValidEmail(String username){

        // 이메일 주소 형식이 아닌 경우 false
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if(!username.matches(regex)){
            return false;
        }

        String domain = username.substring(username.indexOf('@') + 1);

        // 정상적인 이메일 이라면 MX 레코드를 갖는다.
        // Dns를 이용해 MX 레코드를 조회
        // 참조 - https://velog.io/@danielyang-95/%EC%9D%B4%EB%A9%94%EC%9D%BC-%EC%9C%A0%ED%9A%A8%EC%84%B1-%EA%B2%80%EC%A6%9D-by-MX-%EB%A0%88%EC%BD%94%EB%93%9C
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

            DirContext dirContext = new InitialDirContext(env);
            Attributes attrs = dirContext.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");

            // MX 레코드가 존재하면 true 반환
            return attr != null && attr.size() > 0;
        } catch (NamingException e) {
            return false;
        }
    }

}
