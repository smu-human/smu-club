package com.example.smu_club.auth.security;

import com.example.smu_club.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Member member;

    //member Table의 pk
    //public Long getMemberId(){return member.getId();}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())); //enum to string
    }

    @Override
    public String getPassword(){
        return null;
    }

    //학번
    @Override
    public String getUsername(){
        return member.getStudentId();
    }

    //계정 만료 확인 (ture : 만료 안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //계정 잠겨있는지 확인 (true : 잠겨있지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //비밀번호 만료인지 확인(ture : 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // 계정이 활성화(사용가능)인지 리턴 (true: 활성화)
    public boolean isEnabled(){
        return true;
    }



}
