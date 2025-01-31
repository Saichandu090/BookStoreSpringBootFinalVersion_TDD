package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.serviceimpl.MyUserDetailsService;
import com.example.bookstore.util.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest
{
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    private User user;

    @Test
    void loadUserByUsernameValidTestWithUserRole()
    {
        user=User.builder().email("saichandu@gmail.com").password("saichandu@45").role("user").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails userDetails=myUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())));
        assertEquals(user.getEmail(),userDetails.getUsername());
        assertEquals(user.getPassword(),userDetails.getPassword());
    }


    @Test
    void loadUserByUsernameValidTestWithAdminRole()
    {
        user=User.builder().email("dinesh@gmail.com").password("dinesh@45").role("admin").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails userDetails=myUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())));
        assertEquals(user.getEmail(),userDetails.getUsername());
        assertEquals(user.getPassword(),userDetails.getPassword());
    }


    @Test
    void loadUserByUsernameIfUserNotProvideGiveRole()
    {
        user=User.builder().email("dinesh@gmail.com").password("dinesh@45").role("").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails userDetails=myUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())));
        assertEquals(user.getEmail(),userDetails.getUsername());
        assertEquals(user.getPassword(),userDetails.getPassword());
    }

    @Test
    void loadUserByUsernameIfUserRoleIsMissingItShouldFallToUser()
    {
        user=User.builder().email("dinesh@gmail.com").password("dinesh@45").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails userDetails=myUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())));
        assertEquals(user.getEmail(),userDetails.getUsername());
        assertEquals(user.getPassword(),userDetails.getPassword());
    }
}