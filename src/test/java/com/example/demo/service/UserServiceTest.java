package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.BadCredentialsException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.UserLogin;
import com.example.demo.requestdto.UserRegister;
import com.example.demo.responsedto.LoginResponse;
import com.example.demo.responsedto.RegisterResponse;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private UserRegister registerDTO;
    private UserLogin userLogin;
    private UserDetails userDetails;

    @BeforeEach
    public void init()
    {
        registerDTO= UserRegister.builder()
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

        userLogin = UserLogin.builder()
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
    public void userServiceRegisterUserTestMustPassWithValidBody()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<ResponseStructure<RegisterResponse>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(user.getEmail(),response.getBody().getData().getEmail());
        assertEquals(user.getRole(),response.getBody().getData().getRole());
    }

    @Test
    public void userServiceRegisterUserTestIfUserAlreadyExists()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<ResponseStructure<RegisterResponse>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(),response.getBody().getStatus());
    }


    @Test
    public void userServiceLoginUserValidScene()
    {
        when(userRepository.existsByEmail(userLogin.getEmail())).thenReturn(true);

        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword());
        Authentication authenticationResult= Mockito.mock(Authentication.class);

        when(authenticationResult.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationResult);

        when(jwtService.generateToken(userLogin.getEmail())).thenReturn("jwt-token");

        when(context.getBean(MyUserDetailsService.class)).thenReturn(myUserDetailsService);
        when(myUserDetailsService.loadUserByUsername(userLogin.getEmail())).thenReturn(userDetails);

        ResponseEntity<ResponseStructure<LoginResponse>> response=userService.login(userLogin);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("jwt-token",response.getBody().getMessage());
        assertEquals(userLogin.getEmail(),response.getBody().getData().getEmail());
    }


    @Test
    public void userServiceLoginUserIfUserNotRegistered()
    {
        when(userRepository.existsByEmail(userLogin.getEmail())).thenReturn(false);

        assertThrows(UserNotFoundException.class,()->userService.login(userLogin));

        verify(userRepository,times(1)).existsByEmail(anyString());
    }

    @Test
    public void userServiceLoginUserIfAuthenticationFails()
    {
        when(userRepository.existsByEmail(userLogin.getEmail())).thenReturn(true);

        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword());
        Authentication authenticationResult= Mockito.mock(Authentication.class);

        when(authenticationResult.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationResult);

        assertThrows(BadCredentialsException.class,()->userService.login(userLogin));
    }


    @Test
    public void userServiceIsUserExistsIfUserExist()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.isUserExists("marrisaichandu143@gmail.com");
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("User exists",response.getBody().getMessage());
        assertTrue(response.getBody().getData());
    }


    @Test
    public void userServiceIsUserExistsIfUserNotExist()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.isUserExists("marrisaichandu143@gmail.com");
        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
        assertEquals("User not exists",response.getBody().getMessage());
        assertFalse(response.getBody().getData());
    }

    @Test
    public void userServiceForgetPasswordIfUserExist()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.forgetPassword(user.getEmail(),"chandu@090");
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(user.getEmail()+" password updated successfully",response.getBody().getMessage());
        assertTrue(response.getBody().getData());
    }

    @Test
    public void userServiceForgetPasswordIfUserNotExists()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->userService.forgetPassword(user.getEmail(),"chandu@090"));

        verify(userRepository,times(1)).findByEmail(anyString());
    }
}