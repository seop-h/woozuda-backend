package com.woozuda.backend.security.oauth2;

import com.woozuda.backend.account.transdata.OAuth2Response;
import com.woozuda.backend.account.transdata.OAuth2ResponseFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth2ResponseFactoryUnitTest {

    // oauth2 소셜 로그인 단위 테스트
    // 테스트 구간 -  CustomUserDetailsService 의 일부 : oAuth2Response = OAuth2ResponseFactory.getOAuth2Response(provider, oAuth2User.getAttributes());

    @Test
    public void 네이버_로그인() throws Exception {
        // naver: {resultcode=00, message=success, response={id=oz7UDpVICFBzNmLB-mwZHzgR7IIY7-Y02Az8vYPMwyY, email=enjoying1018@naver.com, name=이동현}}
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("resultcode", "00");
        attribute.put("message", "success");


        Map<String, Object> response = new HashMap<>();
        response.put("id", "oz7UDpVICFBzNmLB-mwZHzgR7IIY7-Y02Az8vYPMwyY");
        response.put("email", "enjoying1018@naver.com");
        response.put("name", "이동현");
        attribute.put("response", response);

        OAuth2Response oauth2Response = OAuth2ResponseFactory.getOAuth2Response("naver", attribute);

        assertThat(oauth2Response.getProvider()).isEqualTo("naver");
        assertThat(oauth2Response.getEmail()).isEqualTo("enjoying1018@naver.com");
        assertThat(oauth2Response.getProviderId()).isEqualTo("oz7UDpVICFBzNmLB-mwZHzgR7IIY7-Y02Az8vYPMwyY");
    }

    @Test
    public void 구글_로그인() throws Exception {
        //google : {sub=114239676830803412592, name=Dong Hyeon Lee, given_name=Dong Hyeon, family_name=Lee, picture=https://lh3.googleusercontent.com/a/ACg8ocIkBClm_OfS47EdZK209e7dnd-ZcSiJjLvJHQjhoTx5CGcGrw=s96-c, email=ske04186@gmail.com, email_verified=true}
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("sub", "114239676830803412592");
        attribute.put("name", "Dong Hyeon Lee");
        attribute.put("family_name", "Lee");
        attribute.put("picture", "https://lh3.googleusercontent.com/a/ACg8ocIkBClm_OfS47EdZK209e7dnd-ZcSiJjLvJHQjhoTx5CGcGrw=s96-c");
        attribute.put("email", "ske04186@gmail.com");
        attribute.put("email_verified", true);

        OAuth2Response oauth2Response = OAuth2ResponseFactory.getOAuth2Response("google", attribute);

        assertThat(oauth2Response.getProvider()).isEqualTo("google");
        assertThat(oauth2Response.getEmail()).isEqualTo("ske04186@gmail.com");
        assertThat(oauth2Response.getProviderId()).isEqualTo("114239676830803412592");

    }

    @Test
    public void 카카오_로그인() throws Exception {
        // kakao : {id=3821328965, connected_at=2024-12-05T07:19:11Z, properties={nickname=이동현}, kakao_account={profile_nickname_needs_agreement=false, profile={nickname=이동현, is_default_nickname=false},
        // has_email=true, email_needs_agreement=false, is_email_valid=true, is_email_verified=true, email=enjoying1018@daum.net}}
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("id", "3821328965");
        attribute.put("connected_at", "2024-12-05T07:19:11Z");


        Map<String, Object> properties = new HashMap<>();
        properties.put("nickname", "이동현");
        attribute.put("properties", properties);

        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("profile_nickname_needs_agrrement", false);

        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "이동현");
        profile.put("is_default_nickname", false);
        kakaoAccount.put("profile", profile);

        kakaoAccount.put("has_email", true);
        kakaoAccount.put("email_needs_agreement", false);
        kakaoAccount.put("is_email_valid", true);
        kakaoAccount.put("is_email_verified", true);
        kakaoAccount.put("email", "enjoying1018@daum.net");

        attribute.put("kakao_account", kakaoAccount);

        OAuth2Response oauth2Response = OAuth2ResponseFactory.getOAuth2Response("kakao", attribute);

        assertThat(oauth2Response.getProvider()).isEqualTo("kakao");
        assertThat(oauth2Response.getEmail()).isEqualTo("enjoying1018@daum.net");
        assertThat(oauth2Response.getProviderId()).isEqualTo("3821328965");
    }
}
