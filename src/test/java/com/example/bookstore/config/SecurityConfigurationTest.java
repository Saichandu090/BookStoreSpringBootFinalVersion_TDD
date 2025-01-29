package com.example.bookstore.config;

import com.example.bookstore.entity.User;
import com.example.bookstore.serviceimpl.MyUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest
{
    private SecurityConfiguration securityConfiguration;

    @Mock
    private MyUserDetailsService userDetailsService;

    @Mock
    private CustomCORSConfiguration customCORSConfiguration;

    @Mock
    private JWTFilter jwtFilter;

    @Mock
    private HttpSecurity http;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private MockMvc mockMvc;
    private User user;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp()
    {
        securityConfiguration=new SecurityConfiguration(userDetailsService,customCORSConfiguration,jwtFilter);
        user= User.builder()
                .email("test@example.com")
                .userId(100L)
                .password("Password123")
                .dob(LocalDate.of(1999,8,12))
                .firstName("Mock")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();
    }

    @Test
    void authenticationProviderTestShouldCreateCorrectConfiguredProvider()
    {
        PasswordEncoder passwordEncoder=securityConfiguration.passwordEncoder();
        String encodedPassword=passwordEncoder.encode(user.getPassword());
        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities()
            {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return encodedPassword;
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };

        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        AuthenticationProvider authenticationProvider=securityConfiguration.authenticationProvider();
        Authentication authentication=authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword()));

        assertNotNull(authenticationProvider,"Provider should not be null");
        assertTrue(authentication.isAuthenticated());
        assertEquals(user.getEmail(),authentication.getName());

        verify(userDetailsService).loadUserByUsername(user.getEmail());
    }

    @Test
    void authenticationManagerTestShouldReturnCorrectAuthenticationManager() throws Exception
    {
        AuthenticationManager authenticationManager=mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager actualManager=securityConfiguration.authenticationManager(authenticationConfiguration);
        assertEquals(authenticationManager,actualManager,"Both should match");
        verify(authenticationConfiguration).getAuthenticationManager();
    }


    @Test
    void securityFilterChainTestShouldConfigureSecurityCorrectly() throws Exception
    {
        HttpSecurity httpSecurity=mock(HttpSecurity.class,RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);

        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(),any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(filterChain);

        SecurityFilterChain securityFilterChain=securityConfiguration.securityFilterChain(httpSecurity);

        assertNotNull(securityFilterChain,"Filter should not be null");

        //verifying the method is called or not
        verify(httpSecurity, times(1)).csrf(any());
        verify(httpSecurity, times(1)).authorizeHttpRequests(any());
        verify(httpSecurity, times(1)).cors(any());
        verify(httpSecurity, times(1)).httpBasic(any());
        verify(httpSecurity, times(1)).formLogin(any());
        verify(httpSecurity, times(1)).sessionManagement(any());
        verify(httpSecurity, times(1)).addFilterBefore(eq(jwtFilter), eq(UsernamePasswordAuthenticationFilter.class));
        verify(httpSecurity, times(1)).build();
    }
}