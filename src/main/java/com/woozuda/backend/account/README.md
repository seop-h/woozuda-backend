# Flow 설명 
### 회원 가입
JoinController -> JoinService -> userRepository

### 로그인 - 일반로그인
LoginFilter -> CustomUserDetailsService -> userRepository

### 로그인 - 소셜로그인
(..앞쪽은 시큐리티가 지원..) -> CustomOAuth2UserService -> (성공시) CustomSuccessHandler(security 폴더에 위치)

### jwt 토큰 검증
JWTFilter(security 폴더에 위치)



로그인 후에는 /account/sample/login 에 접속해서 admin controller 이 잘 출력 되는지 확인 하면 됩니다.

쿠키 만료 시간은 3개월 입니다 
