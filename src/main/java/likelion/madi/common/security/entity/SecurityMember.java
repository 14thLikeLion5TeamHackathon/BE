package likelion.madi.common.security.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import likelion.madi.domain.User;
import lombok.Getter;

@Getter
public class SecurityMember implements UserDetails {

    private final User user;

    public SecurityMember(User user) {
        this.user = user;
    }

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        // 소셜 로그인 전용이라 비밀번호를 사용하지 않습니다.
        return "";
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getUserId());
    }

    public static SecurityMember from(User user) {
        return new SecurityMember(user);
    }
}
