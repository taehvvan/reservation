package com.example.backend.login.security;

import com.example.backend.login.LoginMapper;
import com.example.backend.register.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService를 대체하는 새로운 서비스.
 * 로그인 요청 시 DB에서 사용자를 찾아 PrincipalDetails 객체로 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final LoginMapper loginMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = loginMapper.findByEmail(email);
        
        if (userEntity == null) {
            throw new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email);
        }

        // [★★★★★ 최종 디버깅 포인트 ★★★★★]
        // LoginMapper가 DB에서 UserEntity를 조회한 직후, id 필드의 값을 확인합니다.
        System.out.println("---------- [PrincipalDetailsService] DB 조회 직후 상태 확인 ----------");
        System.out.println("조회된 UserEntity 객체: {} : " + userEntity); // UserEntity에 @ToString 필요
        
        if (userEntity.getId() == null) {
            System.out.println("!!!!! 근본 원인 발견: LoginMapper가 UserEntity를 만들 때 id 필드에 null을 할당했습니다.");
            System.out.println("!!!!! 확인 사항: LoginMapper XML 파일의 <resultMap> 또는 UserEntity의 @Column 어노테이션 설정을 확인해주세요.");
        } else {
            System.out.println("✅ UserEntity의 id 필드에 정상적으로 값이 있습니다: {} : " + userEntity.getId());
        }

        // 조회된 UserEntity를 PrincipalDetails 객체에 담아서 반환합니다.
        // 이 객체가 Security Session(Context)에 저장됩니다.
        return new PrincipalDetails(userEntity);
    }
}

