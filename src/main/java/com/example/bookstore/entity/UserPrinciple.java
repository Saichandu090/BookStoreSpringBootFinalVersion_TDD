package com.example.bookstore.entity;

import com.example.bookstore.util.Roles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrinciple implements UserDetails
{
    private User user;

    @Enumerated(EnumType.STRING)
    private Roles role;

    public UserPrinciple(User user)
    {
        this.user=user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        String userRole = (user != null && user.getRole() != null) ? user.getRole().toUpperCase() : Roles.USER.name();
        return Collections.singletonList(new SimpleGrantedAuthority( Roles.ADMIN.name().equals(userRole) ? Roles.ADMIN.name() : Roles.USER.name()));
    }

    @Override
    public String getPassword()
    {
        return user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled()
    {
        return UserDetails.super.isEnabled();
    }
}
