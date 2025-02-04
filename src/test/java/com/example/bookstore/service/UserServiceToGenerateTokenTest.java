package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.exception.BadCredentialsException;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.serviceimpl.JWTService;
import com.example.bookstore.serviceimpl.MyUserDetailsService;
import com.example.bookstore.serviceimpl.UserServiceToGenerateToken;
import com.example.bookstore.util.ResponseStructure;
import com.example.bookstore.util.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceToGenerateTokenTest
{
    @Mock
    private JWTService jwtService;

    @Mock
    private ApplicationContext context;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MyUserDetailsService userDetailsService;

    @InjectMocks
    private UserServiceToGenerateToken userService;

    private UserLoginEntity loginDTO;
    private UserDetails userDetails;
    private UserDetails adminDetails;
    private Authentication authentication;
    private User user;
    private User admin;

    @BeforeEach
    void setUp()
    {
        loginDTO = new UserLoginEntity();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("Password123");

        user= User.builder()
                .email("test@example.com")
                .userId(100L)
                .password("Password123")
                .dob(LocalDate.of(1999,8,12))
                .firstName("Mock")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        admin=User.builder()
                .email("sai@gmail.com")
                .userId(1L)
                .password("saichandu090")
                .dob(LocalDate.of(2002,8,24))
                .firstName("Sai")
                .lastName("Chandu")
                .role("ADMIN")
                .registeredDate(LocalDate.now()).build();

        adminDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(admin.getRole()));
            }

            @Override
            public String getPassword() {
                return admin.getPassword();
            }

            @Override
            public String getUsername() {
                return admin.getEmail();
            }
        };

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities()
            {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };
        authentication = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword(), userDetails.getAuthorities());
    }

    @Test
    void generateTokenWhenValidCredentialsShouldReturnToken()
    {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(loginDTO.getEmail())).thenReturn("test.jwt.token");
        when(context.getBean(MyUserDetailsService.class)).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(loginDTO.getEmail())).thenReturn(userDetails);

        ResponseEntity<ResponseStructure<LoginResponse>> response = userService.generateToken(loginDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        LoginResponse loginResponse = response.getBody().getData();
        assertEquals("test.jwt.token",response.getBody().getMessage());
        assertEquals("test@example.com", loginResponse.getEmail());
        assertEquals(Roles.USER.name(), loginResponse.getRole());
    }

    @Test
    void generateTokenWhenInvalidCredentialsShouldThrowException()
    {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new com.example.bookstore.exception.BadCredentialsException("Bad Credentials"));
        assertThrows(BadCredentialsException.class, () -> userService.generateToken(loginDTO));
    }

    @Test
    void generateTokenWhenAdminRoleShouldReturnAdminToken()
    {
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin.getEmail(), admin.getPassword(), adminDetails.getAuthorities());
        UserLoginEntity adminLogin = new UserLoginEntity();
        adminLogin.setEmail(admin.getEmail());
        adminLogin.setPassword(admin.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(adminAuth);
        when(jwtService.generateToken(adminLogin.getEmail())).thenReturn("admin.jwt.token");
        when(context.getBean(MyUserDetailsService.class)).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(adminLogin.getEmail())).thenReturn(adminDetails);

        ResponseEntity<ResponseStructure<LoginResponse>> response = userService.generateToken(adminLogin);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        LoginResponse loginResponse = response.getBody().getData();
        assertEquals("admin.jwt.token", response.getBody().getMessage());
        assertEquals(adminLogin.getEmail(), loginResponse.getEmail());
        assertEquals(Roles.ADMIN.name(), loginResponse.getRole());
    }

    @Test
    void sendTokenWhenUserRoleShouldReturnUserToken()
    {
        when(jwtService.generateToken(loginDTO.getEmail())).thenReturn("test.jwt.token");
        when(context.getBean(MyUserDetailsService.class)).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(loginDTO.getEmail())).thenReturn(userDetails);

        ResponseEntity<ResponseStructure<LoginResponse>> response = userService.sendToken(loginDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        LoginResponse loginResponse = response.getBody().getData();
        assertEquals("test.jwt.token", response.getBody().getMessage());
        assertEquals(user.getEmail(), loginResponse.getEmail());
        assertEquals(Roles.USER.name(), loginResponse.getRole());
    }
}