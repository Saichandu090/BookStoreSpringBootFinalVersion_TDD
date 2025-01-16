package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.serviceimpl.JWTService;
import com.example.demo.serviceimpl.MyUserDetailsService;
import com.example.demo.serviceimpl.UserServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private MyUserDetailsService myUserDetailsService;

    @Mock
    private UserMapper userMapper;

    private User user;
    private UserRegisterDTO registerDTO;
    private UserLoginDTO userLoginDTO;
    private UserDetails userDetails;

    @BeforeEach
    public void init()
    {
        registerDTO=UserRegisterDTO.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .password("testing")
                .role("ADMIN").build();

        user=User.builder()
                .userId(1L)
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .dob(registerDTO.getDob())
                .email(registerDTO.getEmail())
                .password(registerDTO.getPassword())
                .role(registerDTO.getRole()).build();

        userLoginDTO=UserLoginDTO.builder()
                .email("test@gmail.com")
                .password("testing").build();

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };
    }


    @Test
    public void userService_RegisterUserTest_MustPassWithValidBody()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<ResponseStructure<RegisterResponseDto>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(user.getEmail(),response.getBody().getData().getEmail());
        assertEquals(user.getRole(),response.getBody().getData().getRole());
    }

    @Test
    public void userService_RegisterUserTest_IfUserAlreadyExists()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<ResponseStructure<RegisterResponseDto>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(),response.getBody().getStatus());
    }


    @Test
    public void userService_LoginUser_ValidScene()
    {
        when(userRepository.existsByEmail(userLoginDTO.getEmail())).thenReturn(true);

        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(),userLoginDTO.getPassword());
        Authentication authenticationResult= Mockito.mock(Authentication.class);

        when(authenticationResult.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationResult);

        when(jwtService.generateToken(userLoginDTO.getEmail())).thenReturn("jwt-token");

        when(context.getBean(MyUserDetailsService.class)).thenReturn(myUserDetailsService);
        when(myUserDetailsService.loadUserByUsername(userLoginDTO.getEmail())).thenReturn(userDetails);

        ResponseEntity<ResponseStructure<LoginResponseDto>> response=userService.login(userLoginDTO);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("jwt-token",response.getBody().getMessage());
        assertEquals(userLoginDTO.getEmail(),response.getBody().getData().getEmail());
    }


    @Test
    public void userService_LoginUser_IfUserNotRegistered()
    {
        when(userRepository.existsByEmail(userLoginDTO.getEmail())).thenReturn(false);

        ResponseEntity<ResponseStructure<LoginResponseDto>> response=userService.login(userLoginDTO);

        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(),response.getBody().getStatus());
        assertEquals("User not found",response.getBody().getMessage());
    }

    @Test
    public void userService_LoginUser_IfAuthenticationFails()
    {
        when(userRepository.existsByEmail(userLoginDTO.getEmail())).thenReturn(true);

        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(),userLoginDTO.getPassword());
        Authentication authenticationResult= Mockito.mock(Authentication.class);

        when(authenticationResult.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationResult);

        ResponseEntity<ResponseStructure<LoginResponseDto>> response=userService.login(userLoginDTO);

        assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(),response.getBody().getStatus());
        assertEquals("Bad Credentials",response.getBody().getMessage());
    }
}