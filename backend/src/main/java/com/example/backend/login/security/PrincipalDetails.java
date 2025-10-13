package com.example.backend.login.security;

import com.example.backend.register.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Spring Security가 로그인한 사용자의 정보를 담을 때 사용하는 객체.
 * UserDetails 인터페이스를 구현하며, UserEntity를 통째로 멤버 변수로 가집니다.
 */
public class PrincipalDetails implements UserDetails {

    private final UserEntity user;

    public PrincipalDetails(UserEntity user) {
        this.user = user;
    }

    // 컨트롤러에서 UserEntity 객체를 직접 꺼낼 수 있도록 getter를 제공합니다.
    public UserEntity getUser() {
        return user;
    }

    // 사용자의 권한(Role)을 반환하는 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> user.getRole()); // UserEntity의 role 필드 사용
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security에서 'username'은 고유 식별자를 의미하므로, 이메일을 반환합니다.
        return user.getEmail();
    }

    // 계정 상태 관련 메서드 (특별한 로직이 없다면 모두 true)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

